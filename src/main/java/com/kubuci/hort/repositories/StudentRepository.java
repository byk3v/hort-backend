package com.kubuci.hort.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kubuci.hort.models.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

	@Query("""
		select s.id
		from Student s
		join s.person p
		where (:name is null
		        or lower(p.firstName) like lower(concat('%', :name, '%'))
		        or lower(p.lastName)  like lower(concat('%', :name, '%')))
		  and (:groupId is null or s.group.id = :groupId)
		""")
	List<Long> findIdsByOptionalFilters(@Param("name") String name,
		@Param("groupId") Long groupId);

	@Query("""
		select s
		from Student s
		join fetch s.person p
		join fetch s.group g
		where s.id in :ids
		""")
	List<Student> findByIdIn(@Param("ids") List<Long> ids);
}
