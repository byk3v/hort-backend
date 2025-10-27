package com.kubuci.hort.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.dto.SelfDismissalCreateRequest;
import com.kubuci.hort.dto.SelfDismissalDto;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.SelfDismissal;
import com.kubuci.hort.repositories.SelfDismissalRepository;
import com.kubuci.hort.repositories.StudentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SelfDismissalService {
    private final SelfDismissalRepository repo;
    private final StudentRepository studentRepo;

    @Transactional
    public Long create(SelfDismissalCreateRequest req) {
        var student = studentRepo.findById(req.studentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + req.studentId()));

        var s = new SelfDismissal();
        s.setStudent(student);
        s.setValidFrom(req.validFrom());
        s.setValidUntil(req.validUntil());
        s.setStatus(PermissionStatus.ACTIVE);
        return repo.save(s)
                .getId();
    }

    @Transactional
    public void revoke(Long id) {
        var s = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SelfDismissal not found: " + id));
        if (s.getStatus() == PermissionStatus.REVOKED)
            return;
        s.setStatus(PermissionStatus.REVOKED);
        repo.save(s);
    }

    @Transactional(readOnly = true)
    public List<SelfDismissalDto> listByStudent(Long studentId) {
        return repo.findByStudent_Id(studentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private SelfDismissalDto toDto(SelfDismissal s) {
        return new SelfDismissalDto(s.getId(), s.getStudent()
                .getId(), s.getValidFrom(), s.getValidUntil(), s.getStatus());
    }
}
