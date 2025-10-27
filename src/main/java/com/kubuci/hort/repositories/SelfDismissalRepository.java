package com.kubuci.hort.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.SelfDismissal;

public interface SelfDismissalRepository extends JpaRepository<SelfDismissal, Long> {
    @Query("""
          select s from SelfDismissal s
          where s.student.id = :studentId
            and s.status = 'ACTIVE'
            and s.validFrom <= :at
            and (s.validUntil is null or s.validUntil >= :at)
        """)
    List<SelfDismissal> findActiveFor(@Param("studentId") Long studentId, @Param("at") LocalDateTime at);

    List<SelfDismissal> findByStudent_Id(Long studentId);

    @Query("""
            select sd
            from SelfDismissal sd
            where sd.student.id = :studentId
              and sd.validFrom <= :now
              and (sd.validUntil is null or sd.validUntil >= :now)
        """)
    Optional<SelfDismissal> findActiveForStudentAt(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);
}
