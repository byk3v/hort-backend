package com.kubuci.hort.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.SelfDismissal;

public interface SelfDismissalRepository extends JpaRepository<SelfDismissal, UUID> {
    @Query("""
          select s from SelfDismissal s
          where s.student.id = :studentId
            and s.status = 'ACTIVE'
            and s.validFrom <= :at
            and (s.validUntil is null or s.validUntil >= :at)
        """)
    List<SelfDismissal> findActiveFor(@Param("studentId") UUID studentId, @Param("at") LocalDateTime at);

    List<SelfDismissal> findByStudent_Id(UUID studentId);

    @Query("""
            select sd
            from SelfDismissal sd
            where sd.student.id = :studentId
              and sd.validFrom <= :now
              and (sd.validUntil is null or sd.validUntil >= :now)
        """)
    Optional<SelfDismissal> findActiveForStudentAt(@Param("studentId") UUID studentId, @Param("now") LocalDateTime now);
}
