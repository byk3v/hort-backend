package com.kubuci.hort.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false)
    private Hort hort;

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

}
