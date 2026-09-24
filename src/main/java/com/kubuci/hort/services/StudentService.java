package com.kubuci.hort.services;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.Collector;
import com.kubuci.hort.models.Hort;
import com.kubuci.hort.models.HortGroup;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.GroupRepository;
import com.kubuci.hort.repositories.PersonRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.StudentRepository;
import com.kubuci.hort.security.TenantHortResolver;
import com.kubuci.hort.shared.api.PageResponse;
import com.kubuci.hort.students.api.CollectorOnboardingSource;
import com.kubuci.hort.students.api.NewCollectorRequest;
import com.kubuci.hort.students.api.StudentCollectorOnboardingRequest;
import com.kubuci.hort.students.api.StudentCollectorV1Dto;
import com.kubuci.hort.students.api.StudentGroupV1Dto;
import com.kubuci.hort.students.api.StudentOnboardingV1Request;
import com.kubuci.hort.students.api.StudentV1Dto;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;
	private final PickupRightRepository pickupRightRepository;
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;
	private final CollectorRepository collectorRepository;
	private final TenantHortResolver tenantHortResolver;

	@Transactional(readOnly = true)
	public PageResponse<StudentV1Dto> listV1(String name, UUID groupId, int page, int size, String sort) {
		String nameFilter = name == null || name.isBlank()
			? null
			: name.trim()
				.toLowerCase(Locale.ROOT);
		Specification<Student> filters = (root, query, criteria) -> {
			var person = root.join("person", JoinType.INNER);
			var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
			if (nameFilter != null) {
				String pattern = "%" + nameFilter + "%";
				predicates.add(criteria.or(criteria.like(criteria.lower(person.get("firstName")), pattern),
					criteria.like(criteria.lower(person.get("lastName")), pattern)));
			}
			if (groupId != null) {
				predicates.add(criteria.equal(root.get("group")
					.get("id"), groupId));
			}
			return criteria.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		};

		String[] sortParts = sort.split(",", 2);
		Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);
		Sort ordering = Sort.by(direction, "person." + sortParts[0])
			.and(Sort.by(direction, "person.firstName"))
			.and(Sort.by(Sort.Direction.ASC, "id"));
		Page<Student> studentPage = studentRepository.findAll(filters, PageRequest.of(page, size, ordering));
		List<UUID> ids = studentPage.stream()
			.map(Student::getId)
			.toList();
		return PageResponse.from(studentPage, toV1Dtos(ids));
	}

	@Transactional(readOnly = true)
	public StudentV1Dto getV1ById(UUID id) {
		studentRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
		return toV1Dtos(List.of(id)).getFirst();
	}

	@Transactional
	public StudentV1Dto onboardV1(StudentOnboardingV1Request req) {
		var hort = tenantHortResolver.requireCurrentHort();
		HortGroup group = groupRepository.findById(req.groupId())
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + req.groupId()));

		Person studentPerson = new Person();
		studentPerson.setHort(hort);
		studentPerson.setFirstName(req.student().firstName());
		studentPerson.setLastName(req.student().lastName());
		studentPerson.setAddress(req.student().address());
		studentPerson.setPhone(req.student().phone());
		personRepository.save(studentPerson);

		Student student = new Student();
		student.setHort(hort);
		student.setPerson(studentPerson);
		student.setGroup(group);
		studentRepository.save(student);

		Set<UUID> existingCollectorIds = new HashSet<>();
		List<PickupRight> rights = new ArrayList<>();
		for (StudentCollectorOnboardingRequest collectorRequest : req.collectors()) {
			Collector collector;
			if (collectorRequest.source() == CollectorOnboardingSource.EXISTING) {
				UUID collectorId = collectorRequest.existingCollectorId();
				if (!existingCollectorIds.add(collectorId)) {
					throw new DataIntegrityViolationException("Collector is repeated in onboarding request");
				}
				collector = collectorRepository.findById(collectorId)
					.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + collectorId));
			} else {
				collector = createCollector(hort, collectorRequest.newCollector());
			}

			PickupRight right = new PickupRight();
			right.setHort(hort);
			right.setStudent(student);
			right.setCollector(collector);
			right.setType(collectorRequest.permissionType());
			right.setStatus(PermissionStatus.ACTIVE);
			right.setValidFrom(collectorRequest.validFrom() == null
				? OffsetDateTime.now(ZoneOffset.UTC)
				: collectorRequest.validFrom());
			right.setValidUntil(collectorRequest.validUntil());
			right.setMainCollector(collectorRequest.mainCollector());
			rights.add(right);
		}
		pickupRightRepository.saveAll(rights);

		return toV1Dtos(List.of(student.getId())).getFirst();
	}

	private Collector createCollector(Hort hort, NewCollectorRequest request) {
		Person person = new Person();
		person.setHort(hort);
		person.setFirstName(request.firstName());
		person.setLastName(request.lastName());
		person.setAddress(request.address());
		person.setPhone(request.phone());
		personRepository.save(person);

		Collector collector = new Collector();
		collector.setHort(hort);
		collector.setPerson(person);
		collector.setCollectorType(CollectorType.COLLECTOR);
		return collectorRepository.save(collector);
	}

	private List<StudentV1Dto> toV1Dtos(List<UUID> studentIds) {
		if (studentIds.isEmpty()) {
			return List.of();
		}
		Map<UUID, Student> students = studentRepository.findByIdIn(studentIds)
			.stream()
			.collect(Collectors.toMap(Student::getId, student -> student));
		Map<UUID, List<StudentCollectorV1Dto>> collectors = pickupRightRepository
			.findAllByStudentIdsWithCollectorPerson(studentIds)
			.stream()
			.collect(Collectors.groupingBy(right -> right.getStudent()
				.getId(), Collectors.mapping(right -> {
					Collector collector = right.getCollector();
					Person person = collector.getPerson();
					return new StudentCollectorV1Dto(collector.getId(), person.getFirstName(), person.getLastName(),
						person.getAddress(), person.getPhone(), collector.getCollectorType(), right.getId(),
						right.isMainCollector());
				}, Collectors.toList())));

		return studentIds.stream()
			.map(id -> {
				Student student = students.get(id);
				if (student == null) {
					throw new EntityNotFoundException("Student not found: " + id);
				}
				Person person = student.getPerson();
				HortGroup group = student.getGroup();
					return new StudentV1Dto(student.getId(), person.getFirstName(), person.getLastName(),
						person.getAddress(), person.getPhone(), new StudentGroupV1Dto(group.getId(), group.getName()),
						collectors.getOrDefault(id, List.of()));
			})
			.toList();
	}

}
