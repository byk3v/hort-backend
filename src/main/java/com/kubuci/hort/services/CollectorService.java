package com.kubuci.hort.services;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollectorService {
    private final CollectorRepository collectorRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public List<CollectorDto> list() {
        return collectorRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectorDto getById(Long id) {
        Collector c = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collector not found: " + id));
        return toDto(c);
    }

    @Transactional
    public Long createWithPerson(CollectorSaveWithPersonRequest req) {
        Person p = new Person();
        p.setFirstName(req.firstName());
        p.setLastName(req.lastName());
        p.setAddress(req.address());
        p.setPhone(req.phone());
        personRepository.save(p);

        Collector c = new Collector();
        c.setPerson(p);
        c.setCollectorType(CollectorType.valueOf(req.collectorType()));
        return collectorRepository.save(c)
                .getId();
    }

    @Transactional
    public void update(Long id, CollectorSaveRequest req) {
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
    public void delete(Long id) {
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
}
