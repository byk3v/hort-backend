package com.kubuci.hort.models;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hort_group", indexes = @Index(name = "idx_hort_group_hort_name", columnList = "hort_id,name"))
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class HortGroup extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hort_group_hort"))
    private Hort hort;

    @Column(name = "name", nullable = false, length = 160)
    private String name;
}
