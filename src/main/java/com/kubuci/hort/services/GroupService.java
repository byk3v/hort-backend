package com.kubuci.hort.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kubuci.hort.dto.GroupDto;
import com.kubuci.hort.dto.GroupSaveRequest;
import com.kubuci.hort.dto.GroupUpdateRequest;
import com.kubuci.hort.models.Group;
import com.kubuci.hort.models.Tutor;
import com.kubuci.hort.repositories.GroupRepository;
import com.kubuci.hort.repositories.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final TutorRepository tutorRepository;

	@Transactional(readOnly = true)
	public List<GroupDto> list() {
		return groupRepository.findAll().stream()
			.map(g -> new GroupDto(g.getId(), g.getName()))
			.toList();
	}

	@Transactional(readOnly = true)
	public GroupDto getById(Long id) {
		Group g = groupRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));
		return new GroupDto(g.getId(), g.getName());
	}

	@Transactional
	public Long save(GroupSaveRequest req) {
		//Tutor tutor = tutorRepository.findById(req.tutorId())
		//	.orElseThrow(() -> new EntityNotFoundException("Tutor not found: " + req.tutorId()));

		Group g = new Group();
		g.setName(req.name());
		//g.setTutor(tutor);

		return groupRepository.save(g).getId();
	}

	@Transactional
	public void update(Long id, GroupUpdateRequest req) {
		Group g = groupRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));

		//Tutor tutor = tutorRepository.findById(req.tutorId())
		//	.orElseThrow(() -> new EntityNotFoundException("Tutor not found: " + req.tutorId()));

		g.setName(req.name());
		//g.setTutor(tutor);
		groupRepository.save(g);
	}

	@Transactional
	public void delete(Long id) {
		if (!groupRepository.existsById(id)) {
			throw new EntityNotFoundException("Group not found: " + id);
		}
		groupRepository.deleteById(id);
	}
}
