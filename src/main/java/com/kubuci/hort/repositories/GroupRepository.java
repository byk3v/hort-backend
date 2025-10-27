package com.kubuci.hort.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.HortGroup;

public interface GroupRepository extends JpaRepository<HortGroup, Long> {
}
