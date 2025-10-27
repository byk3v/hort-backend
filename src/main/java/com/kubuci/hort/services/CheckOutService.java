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
	public Long create(CheckOutCreateRequest req) {
		var student = studentRepo.findById(req.studentId())
			.orElseThrow(() -> new EntityNotFoundException("Student not found: " + req.studentId()));

		LocalDateTime at = (req.occurredAt() != null) ? req.occurredAt() : LocalDateTime.now();

		var entity = new CheckOut();
		entity.setStudent(student);
		entity.setOccurredAt(at);
		entity.setComment(req.comment());
		entity.setRecordedByUserId(req.recordedByUserId());

		if (req.collectorType() == CollectorType.COLLECTOR) {
			if (req.collectorId() == null) {
				throw new IllegalArgumentException("collectorId required when collectorType=COLLECTOR");
			}
			var collector = collectorRepo.findById(req.collectorId())
				.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + req.collectorId()));
			// validar permiso activo
			var rights = pickupRightRepo.findActiveFor(student.getId(), at);
			if (rights.isEmpty()) {
				throw new IllegalStateException("No active PickupRight for student/collector at given time");
			}
			entity.setCollectorType(CollectorType.COLLECTOR);
			entity.setCollector(collector);
			entity.setPickupRight(rights.getFirst());
			entity.setSelfDismissal(null);

		} else { // STUDENT (self-dismissal)
			// collectorId no debe venir
			if (req.collectorId() != null) {
				throw new IllegalArgumentException("collectorId must be null when collectorType=STUDENT");
			}
			var perms = selfDismissalRepo.findActiveFor(student.getId(), at);
			if (perms.isEmpty()) {
				throw new IllegalStateException("No active SelfDismissal permission at given time");
			}
			entity.setCollectorType(CollectorType.STUDENT);
			entity.setCollector(null);
			entity.setPickupRight(null);
			entity.setSelfDismissal(perms.getFirst());
		}

		return repo.save(entity).getId();
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


		List<CheckOutStudentInfo> result = matches.stream().map(student -> {
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
							? right.getAllowedFromTime().toString() // "HH:mm:ss"
							: null
					);
				})
				.toList();

			// 3. self-dismissal válido hoy
			SelfDismissal dismissal = selfDismissalRepo
				.findActiveForStudentAt(student.getId(), now)
				.orElse(null);

			boolean canLeaveAloneToday = dismissal != null;
			String allowedToLeaveFromTime = (dismissal != null && dismissal.getAllowedFromTime() != null)
				? dismissal.getAllowedFromTime().toString()
				: null;

			return new CheckOutStudentInfo(
				student.getId(),
				p.getFirstName(),
				p.getLastName(),
				groupName,
				canLeaveAloneToday,
				allowedToLeaveFromTime,
				allowedCollectors
			);
		}).toList();

		return new CheckOutSearchResponse(result);
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
