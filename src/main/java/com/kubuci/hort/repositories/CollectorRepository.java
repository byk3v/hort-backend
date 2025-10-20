package com.kubuci.hort.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kubuci.hort.models.Collector;

public interface CollectorRepository extends JpaRepository<Collector, Long> {
	Optional<Collector> findByPerson_Id(Long personId);

	boolean existsByPerson_Id(Long personId);
}
