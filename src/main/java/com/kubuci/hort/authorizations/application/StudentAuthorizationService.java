package com.kubuci.hort.authorizations.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.authorizations.api.AuthorizationCollectorDto;
import com.kubuci.hort.authorizations.api.AuthorizationCollectorRequest;
import com.kubuci.hort.authorizations.api.AuthorizationStudentDto;
import com.kubuci.hort.authorizations.api.StudentAuthorizationCreateRequest;
import com.kubuci.hort.authorizations.api.StudentAuthorizationDto;
import com.kubuci.hort.authorizations.api.StudentAuthorizationKind;
import com.kubuci.hort.authorizations.api.StudentAuthorizationStatus;
import com.kubuci.hort.authorizations.api.WeeklyAuthorizationRuleDto;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.Collector;
import com.kubuci.hort.models.Hort;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.SelfDismissal;
import com.kubuci.hort.models.SelfDismissalWeeklyRule;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.PersonRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.SelfDismissalRepository;
import com.kubuci.hort.repositories.SelfDismissalWeeklyRuleRepository;
import com.kubuci.hort.repositories.StudentRepository;
import com.kubuci.hort.security.TenantHortResolver;
import com.kubuci.hort.shared.api.PageResponse;
import com.kubuci.hort.students.api.CollectorOnboardingSource;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentAuthorizationService {

	private final PickupRightRepository pickupRightRepository;
	private final SelfDismissalRepository selfDismissalRepository;
	private final SelfDismissalWeeklyRuleRepository weeklyRuleRepository;
	private final StudentRepository studentRepository;
	private final CollectorRepository collectorRepository;
	private final PersonRepository personRepository;
	private final TenantHortResolver tenantHortResolver;

	@Transactional(readOnly = true)
	public PageResponse<StudentAuthorizationDto> list(String status, UUID studentId, int page, int size) {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		List<SelfDismissal> dismissals = selfDismissalRepository.findAllForAuthorizationView();
		Map<UUID, List<WeeklyAuthorizationRuleDto>> weekly = weeklyRules(dismissals);
		List<StudentAuthorizationDto> all = new ArrayList<>();
		pickupRightRepository.findAllForAuthorizationView().stream()
			.map(right -> toDto(right, now))
			.forEach(all::add);
		dismissals.stream()
			.map(dismissal -> toDto(dismissal, weekly.getOrDefault(dismissal.getId(), List.of()), now))
			.forEach(all::add);
		all = all.stream()
			.filter(item -> studentId == null || item.student().id().equals(studentId))
			.filter(item -> status.equals("ALL") || item.status().name().equals(status))
			.sorted(Comparator.comparing(StudentAuthorizationDto::validFrom).reversed()
				.thenComparing(StudentAuthorizationDto::id))
			.toList();
		int from = Math.min(page * size, all.size());
		int to = Math.min(from + size, all.size());
		int totalPages = all.isEmpty() ? 0 : (all.size() + size - 1) / size;
		return new PageResponse<>(all.subList(from, to), page, size, all.size(), totalPages);
	}

	@Transactional(readOnly = true)
	public StudentAuthorizationDto get(StudentAuthorizationKind kind, UUID id) {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		if (kind == StudentAuthorizationKind.PICKUP_RIGHT) {
			return toDto(pickupRightRepository.findById(id)
				.orElseThrow(() -> notFound(kind, id)), now);
		}
		SelfDismissal dismissal = selfDismissalRepository.findById(id)
			.orElseThrow(() -> notFound(kind, id));
		List<WeeklyAuthorizationRuleDto> rules = weeklyRuleRepository.findBySelfDismissal_Id(id).stream()
			.map(this::toRuleDto).toList();
		return toDto(dismissal, rules, now);
	}

	@Transactional
	public StudentAuthorizationDto create(StudentAuthorizationCreateRequest request) {
		Hort hort = tenantHortResolver.requireCurrentHort();
		Student student = studentRepository.findById(request.studentId())
			.orElseThrow(() -> new EntityNotFoundException("Student not found: " + request.studentId()));
		if (request.kind() == StudentAuthorizationKind.PICKUP_RIGHT) {
			Collector collector = resolveCollector(hort, request.collector());
			PickupRight right = new PickupRight();
			right.setHort(hort);
			right.setStudent(student);
			right.setCollector(collector);
			right.setType(request.duration());
			right.setValidFrom(request.validFrom());
			right.setValidUntil(request.validUntil());
			right.setAllowedFromTime(request.allowedFromTime());
			right.setStatus(PermissionStatus.ACTIVE);
			right.setMainCollector(false);
			return toDto(pickupRightRepository.save(right), OffsetDateTime.now(ZoneOffset.UTC));
		}

		SelfDismissal dismissal = new SelfDismissal();
		dismissal.setHort(hort);
		dismissal.setStudent(student);
		dismissal.setValidFrom(request.validFrom());
		dismissal.setValidUntil(request.validUntil());
		dismissal.setAllowedFromTime(request.allowedFromTime());
		dismissal.setStatus(PermissionStatus.ACTIVE);
		selfDismissalRepository.save(dismissal);
		List<SelfDismissalWeeklyRule> rules = request.weeklyRules() == null ? List.of()
			: request.weeklyRules().stream().map(rule -> {
				SelfDismissalWeeklyRule entity = new SelfDismissalWeeklyRule();
				entity.setHort(hort);
				entity.setSelfDismissal(dismissal);
				entity.setDayOfWeek(rule.dayOfWeek());
				entity.setAllowedFromTime(rule.allowedFromTime());
				return entity;
			}).toList();
		weeklyRuleRepository.saveAll(rules);
		return toDto(dismissal, rules.stream().map(this::toRuleDto).toList(),
			OffsetDateTime.now(ZoneOffset.UTC));
	}

	@Transactional
	public void revoke(StudentAuthorizationKind kind, UUID id) {
		if (kind == StudentAuthorizationKind.PICKUP_RIGHT) {
			PickupRight right = pickupRightRepository.findById(id).orElseThrow(() -> notFound(kind, id));
			if (right.getStatus() != PermissionStatus.REVOKED) right.setStatus(PermissionStatus.REVOKED);
			return;
		}
		SelfDismissal dismissal = selfDismissalRepository.findById(id).orElseThrow(() -> notFound(kind, id));
		if (dismissal.getStatus() != PermissionStatus.REVOKED) dismissal.setStatus(PermissionStatus.REVOKED);
	}

	private Collector resolveCollector(Hort hort, AuthorizationCollectorRequest request) {
		if (request.source() == CollectorOnboardingSource.EXISTING) {
			return collectorRepository.findById(request.existingCollectorId())
				.orElseThrow(() -> new EntityNotFoundException(
					"Collector not found: " + request.existingCollectorId()));
		}
		Person person = new Person();
		person.setHort(hort);
		person.setFirstName(request.newCollector().firstName());
		person.setLastName(request.newCollector().lastName());
		person.setAddress(request.newCollector().address());
		person.setPhone(request.newCollector().phone());
		personRepository.save(person);
		Collector collector = new Collector();
		collector.setHort(hort);
		collector.setPerson(person);
		collector.setCollectorType(CollectorType.COLLECTOR);
		return collectorRepository.save(collector);
	}

	private Map<UUID, List<WeeklyAuthorizationRuleDto>> weeklyRules(List<SelfDismissal> dismissals) {
		List<UUID> ids = dismissals.stream().map(SelfDismissal::getId).toList();
		if (ids.isEmpty()) return Map.of();
		return weeklyRuleRepository.findBySelfDismissal_IdIn(ids).stream()
			.collect(Collectors.groupingBy(rule -> rule.getSelfDismissal().getId(),
				Collectors.mapping(this::toRuleDto, Collectors.toList())));
	}

	private StudentAuthorizationDto toDto(PickupRight right, OffsetDateTime now) {
		Person person = right.getCollector().getPerson();
		return new StudentAuthorizationDto(right.getId(), StudentAuthorizationKind.PICKUP_RIGHT,
			student(right.getStudent()), new AuthorizationCollectorDto(right.getCollector().getId(),
				person.getFirstName(), person.getLastName(), person.getPhone()), right.getType(),
			right.getValidFrom(), right.getValidUntil(), right.getAllowedFromTime(), List.of(),
			status(right.getStatus(), right.getValidFrom(), right.getValidUntil(), now));
	}

	private StudentAuthorizationDto toDto(SelfDismissal dismissal, List<WeeklyAuthorizationRuleDto> rules,
		OffsetDateTime now) {
		return new StudentAuthorizationDto(dismissal.getId(), StudentAuthorizationKind.SELF_DISMISSAL,
			student(dismissal.getStudent()), null, inferDuration(dismissal, rules), dismissal.getValidFrom(),
			dismissal.getValidUntil(), dismissal.getAllowedFromTime(), rules,
			status(dismissal.getStatus(), dismissal.getValidFrom(), dismissal.getValidUntil(), now));
	}

	private com.kubuci.hort.enums.PermissionType inferDuration(SelfDismissal dismissal,
		List<WeeklyAuthorizationRuleDto> rules) {
		return rules.isEmpty() ? com.kubuci.hort.enums.PermissionType.DAILY
			: com.kubuci.hort.enums.PermissionType.PERMANENT;
	}

	private AuthorizationStudentDto student(Student student) {
		Person person = student.getPerson();
		return new AuthorizationStudentDto(student.getId(), person.getFirstName(), person.getLastName(),
			student.getGroup().getName());
	}

	private WeeklyAuthorizationRuleDto toRuleDto(SelfDismissalWeeklyRule rule) {
		return new WeeklyAuthorizationRuleDto(rule.getDayOfWeek(), rule.getAllowedFromTime());
	}

	private StudentAuthorizationStatus status(PermissionStatus persisted, OffsetDateTime from,
		OffsetDateTime until, OffsetDateTime now) {
		if (persisted == PermissionStatus.REVOKED) return StudentAuthorizationStatus.REVOKED;
		if (now.isBefore(from)) return StudentAuthorizationStatus.SCHEDULED;
		if (until != null && now.isAfter(until)) return StudentAuthorizationStatus.EXPIRED;
		return StudentAuthorizationStatus.ACTIVE;
	}

	private EntityNotFoundException notFound(StudentAuthorizationKind kind, UUID id) {
		return new EntityNotFoundException("Student authorization not found: " + kind + "/" + id);
	}
}
