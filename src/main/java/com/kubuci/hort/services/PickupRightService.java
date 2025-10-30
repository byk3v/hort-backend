package com.kubuci.hort.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.dto.PickupRightCreateRequest;
import com.kubuci.hort.dto.PickupRightDto;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.PickupRight;
import com.kubuci.hort.repositories.CollectorRepository;
import com.kubuci.hort.repositories.PickupRightRepository;
import com.kubuci.hort.repositories.StudentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PickupRightService {
    private final PickupRightRepository repo;
    private final StudentRepository studentRepo;
    private final CollectorRepository collectorRepo;

    @Transactional
    public UUID create(PickupRightCreateRequest req) {
        var student = studentRepo.findById(req.studentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + req.studentId()));
        var collector = collectorRepo.findById(req.collectorId())
                .orElseThrow(() -> new EntityNotFoundException("Collector not found: " + req.collectorId()));

        var pr = new PickupRight();
        pr.setStudent(student);
        pr.setCollector(collector);
        pr.setType(req.type());
        pr.setValidFrom(req.validFrom());
        pr.setValidUntil(req.validUntil());
        pr.setStatus(PermissionStatus.ACTIVE);
        return repo.save(pr)
                .getId();
    }

    @Transactional
    public void revoke(UUID id) {
        var pr = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PickupRight not found: " + id));
        if (pr.getStatus() == PermissionStatus.REVOKED)
            return;
        pr.setStatus(PermissionStatus.REVOKED);
        repo.save(pr);
    }

    @Transactional(readOnly = true)
    public List<PickupRightDto> listByStudent(UUID studentId) {
        return repo.findByStudent_Id(studentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickupRightDto> listByCollector(UUID collectorId) {
        return repo.findByCollector_Id(collectorId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private PickupRightDto toDto(PickupRight p) {
        return new PickupRightDto(p.getId(), p.getStudent()
                .getId(),
                p.getCollector()
                        .getId(),
                p.getType(), p.getValidFrom(), p.getValidUntil(), p.getStatus());
    }
}
