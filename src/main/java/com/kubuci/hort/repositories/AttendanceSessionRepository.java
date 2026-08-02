package com.kubuci.hort.repositories;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kubuci.hort.models.AttendanceSession;

import jakarta.persistence.LockModeType;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

	boolean existsByStudent_IdAndAttendanceDate(UUID studentId, LocalDate attendanceDate);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from AttendanceSession a where a.id = :id")
	Optional<AttendanceSession> findByIdForUpdate(@Param("id") UUID id);

	@Query(value = """
		select a from AttendanceSession a
		join fetch a.student s join fetch s.person p join fetch s.group g
		where a.attendanceDate = :date and a.checkedOutAt is null
		and (lower(p.firstName) like :pattern
		  or lower(p.lastName) like :pattern
		  or lower(g.name) like :pattern)
		""", countQuery = """
		select count(a) from AttendanceSession a
		join a.student s join s.person p join s.group g
		where a.attendanceDate = :date and a.checkedOutAt is null
		and (lower(p.firstName) like :pattern
		  or lower(p.lastName) like :pattern
		  or lower(g.name) like :pattern)
		""")
	Page<AttendanceSession> findPresent(@Param("date") LocalDate date, @Param("pattern") String pattern,
		Pageable pageable);
}
