package com.kubuci.hort.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kubuci.hort.dto.CheckOutCreateRequest;
import com.kubuci.hort.dto.CheckOutDto;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.CheckOut;
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
