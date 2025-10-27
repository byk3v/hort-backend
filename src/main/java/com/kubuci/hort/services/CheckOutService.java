package com.kubuci.hort.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kubuci.hort.dto.CheckOutCollectorInfo;
import com.kubuci.hort.dto.CheckOutCreateRequest;
import com.kubuci.hort.dto.CheckOutDto;
import com.kubuci.hort.dto.CheckOutSearchResponse;
import com.kubuci.hort.dto.CheckOutStudentInfo;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.CheckOut;
import com.kubuci.hort.models.Collector;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.SelfDismissal;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.CheckOutRepository;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.SelfDismissalRepository;
import com.kubuci.hort.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckOutService {
	private final CheckOutRepository repo;
	private final StudentRepository studentRepo;
	private final CollectorRepository collectorRepo;
	private final PickupRightRepository pickupRightRepo;
	private final SelfDismissalRepository selfDismissalRepo;

	@Transactional
	public void registerCheckout(CheckOutCreateRequest req) {
		var student = studentRepo.findById(req.studentId())
			.orElseThrow(() -> new EntityNotFoundException("Student not found: " + req.studentId()));

		LocalDateTime now = LocalDateTime.now();

		CheckOut abmeldung = new CheckOut();
		abmeldung.setStudent(student);
		abmeldung.setComment(req.comment());
		abmeldung.setOccurredAt(now);

		if (Boolean.TRUE.equals(req.selfDismissal())) {
			// caso: el niño se va solo, aquí no tenemos collectorId ni pickupRightId
			abmeldung.setCollector(null);
			abmeldung.setPickupRight(null);
			abmeldung.setCollectorType(CollectorType.STUDENT);

			var dismissalOpt = selfDismissalRepo.findActiveForStudentAt(student.getId(), now);
			SelfDismissal dismissal = dismissalOpt.orElse(null);
			abmeldung.setSelfDismissal(dismissal);

		} else {
			// caso: lo recoge un adulto autorizado
			if (req.collectorId() == null || req.pickupRightId() == null) {
				throw new IllegalArgumentException("collectorId and pickupRightId required for non-selfDismissal checkout");
			}

			Collector collector = collectorRepo.findById(req.collectorId())
				.orElseThrow(() -> new EntityNotFoundException("Collector not found " + req.collectorId()));

			PickupRight pr = pickupRightRepo.findById(req.pickupRightId())
				.orElseThrow(() -> new EntityNotFoundException("PickupRight not found " + req.pickupRightId()));

			abmeldung.setCollector(collector);
			abmeldung.setPickupRight(pr);
			abmeldung.setSelfDismissal(null);
			abmeldung.setCollectorType(collector.getCollectorType());
		}

		repo.save(abmeldung);
	}

	@Transactional(readOnly = true)
	public List<CheckOutDto> listByStudent(Long studentId) {
		return repo.findByStudent_IdOrderByOccurredAtDesc(studentId).stream()
			.map(this::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CheckOutDto> listByStudentAndDay(Long studentId, LocalDate day) {
		var from = day.atStartOfDay();
		var to = day.plusDays(1).atStartOfDay();
		return repo.findByStudentAndRange(studentId, from, to).stream()
			.map(this::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public CheckOutSearchResponse search(String rawQuery) {
		if (rawQuery == null) {
			return new CheckOutSearchResponse(List.of());
		}

		String normalized = rawQuery.trim().toLowerCase();
		if (normalized.length() < 2) {
			return new CheckOutSearchResponse(List.of());
		}

		LocalDateTime now = LocalDateTime.now();

		String[] parts = normalized.split("\\s+");
		List<Student> matches;
		if (parts.length == 1) {
			matches = studentRepo.searchBySingleTerm(parts[0]);
		} else {
			String t1 = parts[0];
			String t2 = parts[1];
			matches = studentRepo.searchByTwoTerms(t1, t2);
		}


		List<CheckOutStudentInfo> studentsInfo = matches.stream().map(student -> {
			Person p = student.getPerson();
			String groupName = student.getGroup() != null ? student.getGroup().getName() : null;

			// 2. collectors válidos hoy
			List<PickupRight> todaysRights = pickupRightRepo.findActiveForStudentAt(student.getId(), now);

			List<CheckOutCollectorInfo> allowedCollectors = todaysRights.stream()
				.map(right -> {
					Collector c = right.getCollector();
					Person cp = c.getPerson();
					return new CheckOutCollectorInfo(
						c.getId(),
						cp.getFirstName(),
						cp.getLastName(),
						cp.getPhone(),
						right.isMainCollector(),
						right.getAllowedFromTime() != null
							? right.getAllowedFromTime().toString().substring(0,5)
							: null,
						right.getId() // pickupRightId
					);
				})
				.toList();

			var dismissalOpt = selfDismissalRepo.findActiveForStudentAt(student.getId(), now);

			boolean canLeaveAloneToday = dismissalOpt.isPresent();
			String allowedToLeaveFromTime = dismissalOpt
				.map(SelfDismissal::getAllowedFromTime)
				.map(t -> t.toString().substring(0,5))
				.orElse(null);

			Long selfDismissalId = dismissalOpt
				.map(SelfDismissal::getId)
				.orElse(null);

			return new CheckOutStudentInfo(
				student.getId(),
				p.getFirstName(),
				p.getLastName(),
				groupName,
				canLeaveAloneToday,
				allowedToLeaveFromTime,
				selfDismissalId,
				allowedCollectors

			);
		}).toList();

		return new CheckOutSearchResponse(studentsInfo);
	}

	private CheckOutDto toDto(CheckOut c) {
		return new CheckOutDto(
			c.getId(),
			c.getStudent().getId(),
			c.getCollectorType(),
			(c.getCollector() != null ? c.getCollector().getId() : null),
			c.getOccurredAt(),
			(c.getPickupRight() != null ? c.getPickupRight().getId() : null),
			(c.getSelfDismissal() != null ? c.getSelfDismissal().getId() : null),
			c.getComment(),
			c.getRecordedByUserId()
		);
	}
}
