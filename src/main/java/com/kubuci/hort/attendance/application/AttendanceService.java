package com.kubuci.hort.attendance.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.attendance.api.AttendanceCheckOutDto;
import com.kubuci.hort.attendance.api.AttendanceCheckOutRequest;
import com.kubuci.hort.attendance.api.AttendanceCollectorDto;
import com.kubuci.hort.attendance.api.AttendanceSessionDto;
import com.kubuci.hort.attendance.api.AttendanceStatus;
import com.kubuci.hort.attendance.api.AttendanceStudentDto;
import com.kubuci.hort.attendance.api.CheckInRequest;
import com.kubuci.hort.attendance.api.CheckoutMethod;
import com.kubuci.hort.attendance.api.PresentStudentDto;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.AttendanceSession;
import com.kubuci.hort.models.CheckOut;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.SelfDismissal;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.AttendanceSessionRepository;
import com.kubuci.hort.repositories.CheckOutRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.SelfDismissalRepository;
import com.kubuci.hort.repositories.SelfDismissalWeeklyRuleRepository;
import com.kubuci.hort.repositories.StudentRepository;
import com.kubuci.hort.security.TenantContext;
import com.kubuci.hort.security.TenantHortResolver;
import com.kubuci.hort.shared.api.PageResponse;
import com.kubuci.hort.shared.error.ApiConflictException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceSessionRepository attendanceRepository;
	private final StudentRepository studentRepository;
	private final PickupRightRepository pickupRightRepository;
	private final SelfDismissalRepository selfDismissalRepository;
	private final SelfDismissalWeeklyRuleRepository weeklyRuleRepository;
	private final CheckOutRepository checkOutRepository;
	private final TenantHortResolver tenantHortResolver;
	private final TenantContext tenantContext;
	private final HortBusinessTime businessTime;

	@Transactional(readOnly = true)
	public PageResponse<AttendanceStudentDto> checkInCandidates(String name, int page, int size) {
		var pageable = PageRequest.of(page, size, Sort.by("person.lastName", "person.firstName", "id"));
		var result = studentRepository.findCheckInCandidates(businessDate(), searchPattern(name), pageable);
		return PageResponse.from(result, result.getContent().stream().map(this::studentDto).toList());
	}

	@Transactional
	public AttendanceSessionDto checkIn(CheckInRequest request) {
		var hort = tenantHortResolver.requireCurrentHort();
		Student student = studentRepository.findById(request.studentId())
			.orElseThrow(() -> new EntityNotFoundException("Student not found: " + request.studentId()));
		LocalDate date = businessDate();
		if (attendanceRepository.existsByStudent_IdAndAttendanceDate(student.getId(), date)) {
			throw conflict("attendance_already_checked_in", "Student already has an attendance session today.");
		}
		AttendanceSession session = new AttendanceSession();
		session.setHort(hort);
		session.setStudent(student);
		session.setAttendanceDate(date);
		session.setCheckedInAt(now());
		session.setCheckedInByUserId(tenantContext.requireUserId());
		session.setCheckInComment(request.comment());
		try {
			return sessionDto(attendanceRepository.saveAndFlush(session));
		}
		catch (DataIntegrityViolationException exception) {
			throw conflict("attendance_already_checked_in", "Student already has an attendance session today.");
		}
	}

	@Transactional(readOnly = true)
	public PageResponse<PresentStudentDto> presentStudents(String name, int page, int size) {
		var pageable = PageRequest.of(page, size, Sort.by("student.person.lastName", "student.person.firstName"));
		var result = attendanceRepository.findPresent(businessDate(), searchPattern(name), pageable);
		OffsetDateTime at = now();
		LocalTime localTime = businessTime.businessTime();
		return PageResponse.from(result, result.getContent().stream()
			.map(session -> presentDto(session, at, localTime)).toList());
	}

	@Transactional
	public AttendanceCheckOutDto checkOut(AttendanceCheckOutRequest request) {
		AttendanceSession attendance = attendanceRepository.findByIdForUpdate(request.attendanceId())
			.orElseThrow(() -> new EntityNotFoundException("Attendance session not found: " + request.attendanceId()));
		if (attendance.getCheckedOutAt() != null) {
			throw conflict("attendance_already_checked_out", "Attendance session is already checked out.");
		}
		if (!attendance.getAttendanceDate().equals(businessDate())) {
			throw conflict("attendance_not_checked_in", "Student has no open attendance session today.");
		}

		OffsetDateTime at = now();
		LocalTime localTime = businessTime.businessTime();
		Student student = attendance.getStudent();
		CheckOut checkOut = new CheckOut();
		checkOut.setHort(attendance.getHort());
		checkOut.setStudent(student);
		checkOut.setOccurredAt(at);
		checkOut.setComment(request.comment());
		checkOut.setRecordedByUserId(tenantContext.requireUserId());

		if (request.method() == CheckoutMethod.PICKUP) {
			PickupRight right = pickupRightRepository.findById(request.pickupRightId())
				.orElseThrow(() -> new EntityNotFoundException("Pickup right not found: " + request.pickupRightId()));
			if (!right.getStudent().getId().equals(student.getId())
				|| !right.getCollector().getId().equals(request.collectorId())
				|| !isActive(right, at, localTime)) {
				throw authorizationConflict();
			}
			checkOut.setCollector(right.getCollector());
			checkOut.setPickupRight(right);
			checkOut.setSelfDismissal(null);
			checkOut.setCollectorType(right.getCollector().getCollectorType());
		}
		else {
			SelfDismissal dismissal = selfDismissalRepository.findById(request.selfDismissalId())
				.orElseThrow(() -> new EntityNotFoundException(
					"Self-dismissal authorization not found: " + request.selfDismissalId()));
			if (!dismissal.getStudent().getId().equals(student.getId())
				|| !isActive(dismissal, at, localTime)) {
				throw authorizationConflict();
			}
			checkOut.setCollector(null);
			checkOut.setPickupRight(null);
			checkOut.setSelfDismissal(dismissal);
			checkOut.setCollectorType(CollectorType.STUDENT);
		}

		checkOutRepository.save(checkOut);
		attendance.setCheckedOutAt(at);
		attendance.setCheckedOutByUserId(checkOut.getRecordedByUserId());
		attendance.setCheckOut(checkOut);
		attendanceRepository.save(attendance);
		return new AttendanceCheckOutDto(checkOut.getId(), attendance.getId(), student.getId(), request.method(),
			at, request.collectorId(), request.pickupRightId(), request.selfDismissalId(),
			checkOut.getRecordedByUserId(), request.comment());
	}

	private PresentStudentDto presentDto(AttendanceSession session, OffsetDateTime at, LocalTime localTime) {
		UUID studentId = session.getStudent().getId();
		List<AttendanceCollectorDto> collectors = pickupRightRepository.findActiveForStudentAt(studentId, at)
			.stream().filter(right -> isActive(right, at, localTime)).map(right -> {
				var person = right.getCollector().getPerson();
				return new AttendanceCollectorDto(right.getCollector().getId(), person.getFirstName(),
					person.getLastName(), person.getPhone(), right.isMainCollector(), right.getAllowedFromTime(),
					right.getId());
			}).toList();
		SelfDismissal dismissal = selfDismissalRepository.findActiveForStudentAt(studentId, at)
			.filter(item -> isActive(item, at, localTime)).orElse(null);
		return new PresentStudentDto(session.getId(), studentDto(session.getStudent()), session.getCheckedInAt(),
			dismissal != null, allowedTime(dismissal), dismissal == null ? null : dismissal.getId(), collectors);
	}

	private boolean isActive(PickupRight right, OffsetDateTime at, LocalTime localTime) {
		return right.getStatus() == PermissionStatus.ACTIVE && !at.isBefore(right.getValidFrom())
			&& (right.getValidUntil() == null || !at.isAfter(right.getValidUntil()))
			&& (right.getAllowedFromTime() == null || !localTime.isBefore(right.getAllowedFromTime()));
	}

	private boolean isActive(SelfDismissal dismissal, OffsetDateTime at, LocalTime localTime) {
		if (dismissal.getStatus() != PermissionStatus.ACTIVE || at.isBefore(dismissal.getValidFrom())
			|| (dismissal.getValidUntil() != null && at.isAfter(dismissal.getValidUntil()))) return false;
		var rules = weeklyRuleRepository.findBySelfDismissal_Id(dismissal.getId());
		if (rules.isEmpty()) {
			return dismissal.getAllowedFromTime() != null && !localTime.isBefore(dismissal.getAllowedFromTime());
		}
		var today = businessDate().getDayOfWeek();
		return rules.stream().anyMatch(rule -> rule.getDayOfWeek() == today
			&& !localTime.isBefore(rule.getAllowedFromTime()));
	}

	private LocalTime allowedTime(SelfDismissal dismissal) {
		if (dismissal == null) return null;
		var rules = weeklyRuleRepository.findBySelfDismissal_Id(dismissal.getId());
		if (rules.isEmpty()) return dismissal.getAllowedFromTime();
		var today = businessDate().getDayOfWeek();
		return rules.stream().filter(rule -> rule.getDayOfWeek() == today)
			.map(rule -> rule.getAllowedFromTime()).findFirst().orElse(null);
	}

	private AttendanceSessionDto sessionDto(AttendanceSession session) {
		return new AttendanceSessionDto(session.getId(), studentDto(session.getStudent()),
			session.getAttendanceDate(), session.getCheckedInAt(), session.getCheckedInByUserId(),
			session.getCheckInComment(), session.getCheckedOutAt(),
			session.getCheckedOutAt() == null ? AttendanceStatus.PRESENT : AttendanceStatus.CHECKED_OUT);
	}

	private AttendanceStudentDto studentDto(Student student) {
		var person = student.getPerson();
		return new AttendanceStudentDto(student.getId(), person.getFirstName(), person.getLastName(),
			student.getGroup() == null ? null : student.getGroup().getName());
	}

	private String searchPattern(String value) {
		return "%" + (value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT)) + "%";
	}

	private OffsetDateTime now() {
		return businessTime.now();
	}

	private LocalDate businessDate() {
		return businessTime.businessDate();
	}

	private ApiConflictException authorizationConflict() {
		return conflict("checkout_authorization_not_active",
			"The selected checkout authorization is not active for this student and time.");
	}

	private ApiConflictException conflict(String code, String message) {
		return new ApiConflictException(code, message);
	}
}
