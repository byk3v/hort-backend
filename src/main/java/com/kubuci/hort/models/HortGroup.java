package com.kubuci.hort.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hort_group")
public class HortGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
