package com.kubuci.hort.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kubuci.hort.models.Collector;

public interface CollectorRepository extends JpaRepository<Collector, Long> {
	Optional<Collector> findByPerson_Id(Long personId);

	boolean existsByPerson_Id(Long personId);

	@Query("""
        select c
        from Collector c
        where lower(c.person.firstName) = lower(:firstName)
          and lower(c.person.lastName)  = lower(:lastName)
          and (
                (:phone is null and c.person.phone is null)
                or lower(c.person.phone) = lower(:phone)
              )
    """)
	Optional<Collector> findMatch(
		@Param("firstName") String firstName,
		@Param("lastName") String lastName,
		@Param("phone") String phone
	);
}
