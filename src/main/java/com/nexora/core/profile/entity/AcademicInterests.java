package com.nexora.core.profile.entity;


import com.nexora.core.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="intereses_academicos")
public class AcademicInterests extends BaseEntity {
    @Column(name="name", unique = true, nullable = false)
    public String name;
}
