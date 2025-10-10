package com.kubuci.hort.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kubuci.hort.dto.PersonDto;
import com.kubuci.hort.dto.PersonSaveRequest;
import com.kubuci.hort.dto.PersonUpdateRequest;
import com.kubuci.hort.models.Person;
import com.kubuci.hort.repositories.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;

	@Transactional(readOnly = true)
	public List<PersonDto> list() {
		return personRepository.findAll().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public PersonDto getById(Long id) {
		Person p = personRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Person not found: " + id));
		return toDto(p);
	}

	@Transactional
	public Long save(PersonSaveRequest req) {
		Person p = new Person();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAddress(req.address());
		p.setPhone(req.phone());
		return personRepository.save(p).getId();
	}

	@Transactional
	public void update(Long id, PersonUpdateRequest req) {
		Person p = personRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Person not found: " + id));

		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAddress(req.address());
		p.setPhone(req.phone());
		personRepository.save(p);
	}

	@Transactional
	public void delete(Long id) {
		if (!personRepository.existsById(id)) {
			throw new EntityNotFoundException("Person not found: " + id);
		}
		personRepository.deleteById(id);
	}

	private PersonDto toDto(Person p) {
		return new PersonDto(
			p.getId(),
			p.getFirstName(),
			p.getLastName(),
			p.getAddress(),
			p.getPhone()
		);
	}
}
