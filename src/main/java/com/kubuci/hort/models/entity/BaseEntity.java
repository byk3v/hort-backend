package com.kubuci.hort.models.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@ToString
@EqualsAndHashCode
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Setter
    private UUID id;

    @CreatedBy
    @NotNull
    @Column(name = "created_by", nullable = false)
    protected String createdBy;

    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false)
    protected LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 64)
    protected String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    protected LocalDateTime lastModifiedDate;
}
