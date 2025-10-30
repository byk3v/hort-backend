package com.kubuci.hort.models;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "hort_group")
public class HortGroup extends BaseEntity {

    @Column(name = "name", length = 160, nullable = false)
    private String name;

    // @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // @JoinColumn(name = "id_tutor", nullable = false, foreignKey =
    // @ForeignKey(name = "fk_group_tutor"))
    // private Tutor tutor;

    // public Tutor getTutor() {
    // return tutor;
    // }

    // public void setTutor(Tutor tutor) {
    // this.tutor = tutor;
    // }
}
