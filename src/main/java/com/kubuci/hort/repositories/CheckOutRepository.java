package com.kubuci.hort.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.CheckOut;

public interface CheckOutRepository extends JpaRepository<CheckOut, UUID> {

    @Query("""
          select c from CheckOut c
          where c.student.id = :studentId
            and c.occurredAt >= :from and c.occurredAt < :to
          order by c.occurredAt desc
        """)
    List<CheckOut> findByStudentAndRange(@Param("studentId") UUID studentId, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    List<CheckOut> findByStudent_IdOrderByOccurredAtDesc(UUID studentId);

    @Query("""
            select count(c) > 0
            from CheckOut c
            where c.student.id = :studentId
              and date(c.occurredAt) = current_date
        """)
    boolean existsForToday(@Param("studentId") UUID studentId);

}
