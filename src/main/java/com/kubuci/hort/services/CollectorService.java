package com.kubuci.hort.services;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.dto.CollectorDto;
import com.kubuci.hort.dto.CollectorSaveRequest;
import com.kubuci.hort.dto.CollectorSaveWithPersonRequest;
import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.Collector;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.PersonRepository;
import com.kubuci.hort.security.TenantHortResolver;
import com.kubuci.hort.collectors.api.CollectorV1Dto;
import com.kubuci.hort.collectors.api.CollectorWriteRequest;
import com.kubuci.hort.shared.api.PageResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollectorService {
	private final CollectorRepository collectorRepository;
	private final PersonRepository personRepository;
	private final TenantHortResolver tenantHortResolver;

	@Transactional(readOnly = true)
	public List<CollectorDto> list() {
		return collectorRepository.findAll()
			.stream()
			.map(this::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<CollectorV1Dto> listV1(String name, int page, int size) {
		String nameFilter = name == null || name.isBlank() ? null : name.trim().toLowerCase(Locale.ROOT);
		Specification<Collector> filter = (root, query, criteria) -> {
			if (nameFilter == null) return criteria.conjunction();
			var person = root.join("person");
			String pattern = "%" + nameFilter + "%";
			return criteria.or(criteria.like(criteria.lower(person.get("firstName")), pattern),
				criteria.like(criteria.lower(person.get("lastName")), pattern));
		};
		var ordering = Sort.by("person.lastName").ascending()
			.and(Sort.by("person.firstName").ascending())
			.and(Sort.by("id").ascending());
		var result = collectorRepository.findAll(filter, PageRequest.of(page, size, ordering));
		return PageResponse.from(result, result.stream().map(this::toV1Dto).toList());
	}

	@Transactional(readOnly = true)
	public CollectorV1Dto getV1ById(UUID id) {
		return toV1Dto(requireCollector(id));
	}

	@Transactional
	public CollectorV1Dto createV1(CollectorWriteRequest request) {
		var hort = tenantHortResolver.requireCurrentHort();
		Person person = new Person();
		person.setHort(hort);
		apply(request, person);
		personRepository.save(person);

		Collector collector = new Collector();
		collector.setHort(hort);
		collector.setPerson(person);
		collector.setCollectorType(CollectorType.COLLECTOR);
		return toV1Dto(collectorRepository.save(collector));
	}

	@Transactional
	public CollectorV1Dto updateV1(UUID id, CollectorWriteRequest request) {
		Collector collector = requireCollector(id);
		apply(request, collector.getPerson());
		return toV1Dto(collector);
	}

	@Transactional(readOnly = true)
	public CollectorDto getById(UUID id) {
		Collector c = collectorRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + id));
		return toDto(c);
	}

	@Transactional
	public UUID createWithPerson(CollectorSaveWithPersonRequest req) {
		var hort = tenantHortResolver.requireCurrentHort();
		Person p = new Person();
		p.setHort(hort);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAddress(req.address());
		p.setPhone(req.phone());
		personRepository.save(p);

		Collector c = new Collector();
		c.setHort(hort);
		c.setPerson(p);
		c.setCollectorType(CollectorType.valueOf(req.collectorType()));
		return collectorRepository.save(c)
			.getId();
	}

	@Transactional
	public void update(UUID id, CollectorSaveRequest req) {
		Collector c = collectorRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + id));

		Person p = personRepository.findById(req.personId())
			.orElseThrow(() -> new EntityNotFoundException("Person not found: " + req.personId()));

		// Si cambio la persona, respeto unicidad 1:1
		collectorRepository.findByPerson_Id(p.getId())
			.filter(existing -> !existing.getId()
				.equals(id))
			.ifPresent(existing -> {
				throw new DataIntegrityViolationException("Person already linked to a Collector: " + p.getId());
			});

		c.setPerson(p);
		c.setCollectorType(CollectorType.valueOf(req.collectorType()));
		collectorRepository.save(c);
	}

	@Transactional
	public void delete(UUID id) {
		Collector c = collectorRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + id));
		collectorRepository.delete(c);
	}

	private CollectorDto toDto(Collector c) {
		Person p = c.getPerson();
		return new CollectorDto(c.getId(), p.getFirstName(), p.getLastName(), p.getAddress(), p.getPhone(),
			c.getCollectorType()
				.name());
	}

	private Collector requireCollector(UUID id) {
		return collectorRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Collector not found: " + id));
	}

	private void apply(CollectorWriteRequest request, Person person) {
		person.setFirstName(request.firstName());
		person.setLastName(request.lastName());
		person.setAddress(request.address());
		person.setPhone(request.phone());
	}

	private CollectorV1Dto toV1Dto(Collector collector) {
		Person person = collector.getPerson();
		return new CollectorV1Dto(collector.getId(), person.getFirstName(), person.getLastName(),
			person.getAddress(), person.getPhone(), collector.getCollectorType());
	}
}
