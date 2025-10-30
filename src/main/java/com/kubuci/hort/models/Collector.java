package com.kubuci.hort.models;

import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "collector")
public class Collector extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_collector_person"))
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "collector_type", nullable = false, length = 16)
    private CollectorType collectorType;

}
