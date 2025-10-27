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

	@Query("""
        select s
        from Student s
        join s.person p
        left join s.group g
        where lower(p.firstName) like lower(concat('%', :term, '%'))
           or lower(p.lastName)  like lower(concat('%', :term, '%'))
           or lower(g.name)      like lower(concat('%', :term, '%'))
    """)
	List<Student> searchBySingleTerm(@Param("term") String term);

	@Query("""
        select s
        from Student s
        join s.person p
        left join s.group g
        where (
                (lower(p.firstName) like lower(concat('%', :t1, '%'))
              or lower(p.lastName)  like lower(concat('%', :t1, '%'))
              or lower(g.name)      like lower(concat('%', :t1, '%')))
            and
                (lower(p.firstName) like lower(concat('%', :t2, '%'))
              or lower(p.lastName)  like lower(concat('%', :t2, '%'))
              or lower(g.name)      like lower(concat('%', :t2, '%')))
        )
    """)
	List<Student> searchByTwoTerms(
		@Param("t1") String t1,
		@Param("t2") String t2
	);

}
