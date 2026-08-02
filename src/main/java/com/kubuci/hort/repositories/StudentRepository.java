package com.kubuci.hort.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.Student;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

	@Query(value = """
		select s from Student s join fetch s.person p join fetch s.group g
		where (lower(p.firstName) like :pattern
		  or lower(p.lastName) like :pattern
		  or lower(g.name) like :pattern)
		and not exists (select a.id from AttendanceSession a
		  where a.student = s and a.attendanceDate = :date)
		""", countQuery = """
		select count(s) from Student s join s.person p join s.group g
		where (lower(p.firstName) like :pattern
		  or lower(p.lastName) like :pattern
		  or lower(g.name) like :pattern)
		and not exists (select a.id from AttendanceSession a
		  where a.student = s and a.attendanceDate = :date)
		""")
	Page<Student> findCheckInCandidates(@Param("date") java.time.LocalDate date,
		@Param("pattern") String pattern, Pageable pageable);

	@Query("""
        select s.id
        from Student s
        join s.person p
        where (:name is null
                or lower(p.firstName) like lower(concat('%', :name, '%'))
                or lower(p.lastName)  like lower(concat('%', :name, '%')))
          and (:groupId is null or s.group.id = :groupId)
        """)
	List<UUID> findIdsByOptionalFilters(@Param("name") String name, @Param("groupId") UUID groupId);

	@Query("""
        select s
        from Student s
        join fetch s.person p
        join fetch s.group g
        where s.id in :ids
        """)
	List<Student> findByIdIn(@Param("ids") List<UUID> ids);

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
	List<Student> searchByTwoTerms(@Param("t1") String t1, @Param("t2") String t2);

}
