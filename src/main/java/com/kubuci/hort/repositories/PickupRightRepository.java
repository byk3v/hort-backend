package com.kubuci.hort.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.PickupRight;

public interface PickupRightRepository extends JpaRepository<PickupRight, UUID> {
    @Query("""
        select pr
        from PickupRight pr
        join fetch pr.collector c
        join fetch c.person cp
        where pr.student.id in :studentIds
        """)
    List<PickupRight> findAllByStudentIdsWithCollectorPerson(@Param("studentIds") List<UUID> studentIds);

    @Query("""
        select pr
        from PickupRight pr
        join fetch pr.collector c
        join fetch c.person cp
        where pr.student.id in :studentIds
          and pr.status = 'ACTIVE'
          and pr.validFrom <= :at
          and (pr.validUntil is null or pr.validUntil >= :at)
        """)
    List<PickupRight> findAllActiveByStudentIdsWithCollectorPerson(@Param("studentIds") List<UUID> studentIds,
            @Param("at") LocalDateTime at); // en caso de que quiera coger los que tengan derechos activos hoy

    @Query("""
          select p from PickupRight p
          where p.student.id = :studentId
            and p.status = 'ACTIVE'
            and p.validFrom <= :at
            and (p.validUntil is null or p.validUntil >= :at)
        """)
    List<PickupRight> findActiveFor(@Param("studentId") UUID studentId, @Param("at") LocalDateTime at);

    List<PickupRight> findByStudent_Id(UUID studentId);

    List<PickupRight> findByCollector_Id(UUID collectorId);

    @Query("""
            select pr
            from PickupRight pr
            join fetch pr.collector c
            join fetch c.person cp
            where pr.student.id = :studentId
              and pr.validFrom <= :now
              and (pr.validUntil is null or pr.validUntil >= :now)
        """)
    List<PickupRight> findActiveForStudentAt(@Param("studentId") UUID studentId, @Param("now") LocalDateTime now);
}
