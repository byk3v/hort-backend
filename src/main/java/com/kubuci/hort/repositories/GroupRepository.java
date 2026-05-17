package com.kubuci.hort.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.HortGroup;

public interface GroupRepository extends JpaRepository<HortGroup, UUID> {
}
