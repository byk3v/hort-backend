package com.kubuci.hort.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.dto.GroupDto;
import com.kubuci.hort.dto.GroupSaveRequest;
import com.kubuci.hort.dto.GroupUpdateRequest;
import com.kubuci.hort.models.HortGroup;
import com.kubuci.hort.repositories.GroupRepository;
import com.kubuci.hort.security.TenantHortResolver;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final TenantHortResolver tenantHortResolver;

	@Transactional(readOnly = true)
	public List<GroupDto> list() {
		return groupRepository.findAll()
			.stream()
			.map(g -> new GroupDto(g.getId(), g.getName()))
			.toList();
	}

	@Transactional(readOnly = true)
	public GroupDto getById(UUID id) {
		HortGroup g = groupRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));
		return new GroupDto(g.getId(), g.getName());
	}

	@Transactional
	public GroupDto create(GroupSaveRequest req) {
		var hort = tenantHortResolver.requireCurrentHort();
		HortGroup group = new HortGroup();
		group.setHort(hort);
		group.setName(req.name());
		HortGroup saved = groupRepository.save(group);
		return new GroupDto(saved.getId(), saved.getName());
	}

	@Transactional
	public void update(UUID id, GroupUpdateRequest req) {
		HortGroup g = groupRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));

		g.setName(req.name());
		groupRepository.save(g);
	}

	@Transactional
	public void delete(UUID id) {
		if (!groupRepository.existsById(id)) {
			throw new EntityNotFoundException("Group not found: " + id);
		}
		groupRepository.deleteById(id);
	}
}
