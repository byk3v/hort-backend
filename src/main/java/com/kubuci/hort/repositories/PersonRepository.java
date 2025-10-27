package com.kubuci.hort.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
