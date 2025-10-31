package com.kubuci.hort.models;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "person")
public class Person extends BaseEntity {

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "phone", length = 40)
    private String phone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false)
    private Hort hort;

}
