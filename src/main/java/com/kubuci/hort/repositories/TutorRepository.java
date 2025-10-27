package com.kubuci.hort.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
}
