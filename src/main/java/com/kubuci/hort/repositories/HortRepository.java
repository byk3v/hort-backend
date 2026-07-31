package com.kubuci.hort.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.Hort;

public interface HortRepository extends JpaRepository<Hort, UUID> {
}
