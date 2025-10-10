package com.kubuci.hort.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.kubuci.hort.models.PickupRight;

public interface PickupRightRepository extends JpaRepository<PickupRight, Long> {
	// Trae PickupRight + Collector + Person para un conjunto de estudiantes
	@Query("""
      select pr from PickupRight pr
      join fetch pr.collector c
      join fetch c.person cp
      where pr.student.id in :studentIds
      """)
	List<PickupRight> findAllByStudentIdsWithCollectorPerson(List<Long> studentIds);
}
