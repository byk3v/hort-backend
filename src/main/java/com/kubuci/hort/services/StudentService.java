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

import com.kubuci.hort.dto.CollectorDto;
import com.kubuci.hort.dto.CollectorForOnboarding;
import com.kubuci.hort.dto.StudentDto;
import com.kubuci.hort.dto.StudentOnboardingRequest;
import com.kubuci.hort.dto.StudentOnboardingResponse;
import com.kubuci.hort.dto.StudentSaveRequest;
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
	public List<StudentDto> list(String name, UUID groupId) {
		// Paso 1: obtener IDs filtrados (para luego cargar collectors en bloque)
		final String nameFilter = (name == null || name.isBlank())
			? null
			: name.trim();
		final List<UUID> studentIds = (nameFilter == null && groupId == null)
			? studentRepository.findAll()
			.stream()
			.map(Student::getId)
			.toList()
			: studentRepository.findIdsByOptionalFilters(nameFilter, groupId);

		if (studentIds.isEmpty()) {
			return List.of();
		}

		// Paso 2: cargar Students con person y group (evitar N+1)
		final List<Student> students = studentRepository.findByIdIn(studentIds);

		// Paso 3: cargar PickupRight + Collector(Person) de todos esos students en 1
		// query
		final List<PickupRight> rights = pickupRightRepository.findAllByStudentIdsWithCollectorPerson(studentIds);

		// Mapear studentId -> collectors
		final HashMap<UUID, List<CollectorDto>> collectorsByStudent = rights.stream()
			.collect(Collectors.groupingBy(pr -> pr.getStudent()
				.getId(), HashMap::new, Collectors.mapping(pr -> {
				var cp = pr.getCollector()
					.getPerson();
				return new CollectorDto(pr.getCollector()
					.getId(), cp.getFirstName(), cp.getLastName(), cp.getAddress(), cp.getPhone(),
					pr.getCollector()
						.getCollectorType()
						.name());
			}, Collectors.toCollection(ArrayList::new))));

		// Mantener el orden de studentIds con un LinkedHashMap
		var order = new LinkedHashMap<UUID, Integer>();
		for (int i = 0; i < studentIds.size(); i++) {
			order.put(studentIds.get(i), i);
		}

		return students.stream()
			.sorted((a, b) -> Integer.compare(order.getOrDefault(a.getId(), Integer.MAX_VALUE),
				order.getOrDefault(b.getId(), Integer.MAX_VALUE)))
			.map(s -> {
				var p = s.getPerson();
				var g = s.getGroup();
				var colls = collectorsByStudent.getOrDefault(s.getId(), List.of());
				return new StudentDto(s.getId(), p.getFirstName(), p.getLastName(), p.getAddress(), g.getName(),
					colls);
			})
			.toList();
	}

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

	@Transactional
	public UUID save(StudentSaveRequest req) {
		var hort = tenantHortResolver.requireCurrentHort();
		Person p = new Person();
		p.setHort(hort);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAddress(req.address());
		p.setPhone(req.phone());
		personRepository.save(p);

		HortGroup group = groupRepository.findById(req.groupId())
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + req.groupId()));

		Student student = new Student();
		student.setHort(hort);
		student.setPerson(p);
		student.setGroup(group);

		Student saved = studentRepository.save(student);
		return saved.getId();
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

	@Transactional
	public StudentOnboardingResponse onboardNewStudent(StudentOnboardingRequest req) {
		var hort = tenantHortResolver.requireCurrentHort();
		Person studentData = new Person();
		studentData.setHort(hort);
		studentData.setFirstName(req.student()
			.firstName());
		studentData.setLastName(req.student()
			.lastName());
		studentData.setAddress(req.student()
			.address());
		personRepository.save(studentData);

		HortGroup group = groupRepository.findById(req.groupId())
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + req.groupId()));

		Student student = new Student();
		student.setHort(hort);
		student.setPerson(studentData);
		student.setGroup(group);
		studentRepository.save(student);

		// Para cada collector del request:
		List<Collector> collectorEntities = new java.util.ArrayList<>();
		List<PickupRight> rightsToSave = new java.util.ArrayList<>();

		for (CollectorForOnboarding cReq : req.collectors()) {

			// buscar collector existente
			Collector collector = collectorRepository.findMatch(cReq.firstName(), cReq.lastName(), cReq.phone())
				.orElseGet(() -> {
					// no existe -> creamos Person y Collector
						Person collectorPerson = new Person();
						collectorPerson.setHort(hort);
					collectorPerson.setFirstName(cReq.firstName());
					collectorPerson.setLastName(cReq.lastName());
					collectorPerson.setAddress(cReq.address());
					collectorPerson.setPhone(cReq.phone());
					personRepository.save(collectorPerson);

						Collector newCollector = new Collector();
						newCollector.setHort(hort);
					newCollector.setCollectorType(cReq.type());
					newCollector.setPerson(collectorPerson);

					return collectorRepository.save(newCollector);
				});

			collectorEntities.add(collector);

			PickupRight right = new PickupRight();
			right.setHort(hort);
			right.setStudent(student);
			right.setCollector(collector);
			right.setType(cReq.permissionType());
			right.setStatus(PermissionStatus.ACTIVE);
			OffsetDateTime effectiveFrom = cReq.validFrom() != null
				? cReq.validFrom()
				: OffsetDateTime.now(ZoneOffset.UTC);
			OffsetDateTime effectiveUntil = cReq.validUntil() != null
				? cReq.validUntil()
				: null;
			right.setValidFrom(effectiveFrom);
			right.setValidUntil(effectiveUntil);
			right.setMainCollector(cReq.mainCollector());
			rightsToSave.add(right);
		}

		pickupRightRepository.saveAll(rightsToSave);

		List<UUID> collectorIds = collectorEntities.stream()
			.map(Collector::getId)
			.toList();

		List<UUID> pickupRightIds = rightsToSave.stream()
			.map(PickupRight::getId)
			.toList();

		return new StudentOnboardingResponse(student.getId(), collectorIds, pickupRightIds);
	}
}
