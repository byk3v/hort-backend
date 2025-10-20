package com.kubuci.hort.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kubuci.hort.dto.CollectorDto;
import com.kubuci.hort.dto.StudentDto;
import com.kubuci.hort.dto.StudentSaveRequest;
import com.kubuci.hort.models.Group;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.models.Student;
import com.kubuci.hort.repositories.GroupRepository;
import com.kubuci.hort.repositories.PersonRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;
	private final PickupRightRepository pickupRightRepository;
	private final PersonRepository personRepository;
	private final GroupRepository groupRepository;

	@Transactional(readOnly = true)
	public List<StudentDto> list(String name, Long groupId) {
		// Paso 1: obtener IDs filtrados (para luego cargar collectors en bloque)
		final String nameFilter = (name == null || name.isBlank()) ? null : name.trim();
		final List<Long> studentIds = (nameFilter == null && groupId == null)
			? studentRepository.findAll().stream().map(Student::getId).toList()
			: studentRepository.findIdsByOptionalFilters(nameFilter, groupId);

		if (studentIds.isEmpty()) {
			return List.of();
		}

		// Paso 2: cargar Students con person y group (evitar N+1)
		final List<Student> students = studentRepository.findByIdIn(studentIds);

		// Paso 3: cargar PickupRight + Collector(Person) de todos esos students en 1 query
		final List<PickupRight> rights = pickupRightRepository.findAllByStudentIdsWithCollectorPerson(studentIds);

		// Mapear studentId -> collectors
		final HashMap<Long, List<CollectorDto>> collectorsByStudent =
			rights.stream().collect(Collectors.groupingBy(
				pr -> pr.getStudent().getId(),
				HashMap::new,
				Collectors.mapping(pr -> {
					var cp = pr.getCollector().getPerson();
					return new CollectorDto(
						pr.getCollector().getId(),
						cp.getFirstName(),
						cp.getLastName(),
						cp.getAddress(),
						cp.getPhone(),
						pr.getCollector().getCollectorType().name()
					);
				}, Collectors.toCollection(ArrayList::new))
			));

		// Mantener el orden de studentIds con un LinkedHashMap si quisieras
		var order = new LinkedHashMap<Long, Integer>();
		for (int i = 0; i < studentIds.size(); i++) order.put(studentIds.get(i), i);

		return students.stream()
			.sorted((a, b) -> Integer.compare(order.getOrDefault(a.getId(), Integer.MAX_VALUE),
				order.getOrDefault(b.getId(), Integer.MAX_VALUE)))
			.map(s -> {
				var p = s.getPerson();
				var g = s.getGroup();
				var colls = collectorsByStudent.getOrDefault(s.getId(), List.of());
				return new StudentDto(
					s.getId(),
					p.getFirstName(),
					p.getLastName(),
					p.getAddress(),
					g.getName(),
					colls
				);
			})
			.toList();
	}

	@Transactional
	public Long save(StudentSaveRequest req) {
		Person p = new Person();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAddress(req.address());
		p.setPhone(req.phone());
		personRepository.save(p);

		Group group = groupRepository.findById(req.groupId())
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + req.groupId()));

		Student student = new Student();
		student.setPerson(p);
		student.setGroup(group);

		Student saved = studentRepository.save(student);
		return saved.getId();
	}
}
