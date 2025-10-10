package com.kubuci.hort.repositories;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.kubuci.hort.models.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
	// Búsqueda por nombre (first o last) en la entidad Person del Student
	@Query("""
      select s from Student s
      join fetch s.person p
      join s.group g
      where (lower(p.firstName) like lower(concat('%', :name, '%'))
             or lower(p.lastName) like lower(concat('%', :name, '%')))
        and (:groupId is null or g.id = :groupId)
      """)
	List<Student> searchByNameAndGroup(String name, Long groupId);

	// Listado (sin filtro) – cargamos person y group para evitar N+1
	@Query("""
      select s from Student s
      join fetch s.person p
      join fetch s.group g
      """)
	List<Student> findAllWithPersonAndGroup();

	// Útiles para la segunda fase (colecciones): traer ids a partir de filtros
	@Query("""
      select s.id from Student s
      join s.person p
      join s.group g
      where (:name is null
             or lower(p.firstName) like lower(concat('%', :name, '%'))
             or lower(p.lastName)  like lower(concat('%', :name, '%')))
        and (:groupId is null or g.id = :groupId)
      """)
	List<Long> findIdsByOptionalFilters(String name, Long groupId);

	List<Student> findByIdIn(Collection<Long> ids);
}
