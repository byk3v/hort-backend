package com.kubuci.hort.models;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "hort", uniqueConstraints = { @UniqueConstraint(name = "uk_hort_name", columnNames = { "name" }) })
public class Hort {

    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    public Hort(UUID keycloakId, String name) {
        this.id = keycloakId;
        this.name = name;
    }

    protected Hort() {} // JPA

    // to use in service-> var hort = new Hort(kcGroupId, "Hort Demo Leipzig");
}
