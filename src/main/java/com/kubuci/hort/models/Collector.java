package com.kubuci.hort.models;

import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "collector", indexes = @Index(name = "idx_collector_hort", columnList = "hort_id"))
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Collector extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false, foreignKey = @ForeignKey(name = "fk_collector_hort"))
    private Hort hort;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_collector_person"))
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "collector_type", nullable = false)
    private CollectorType collectorType;
}
