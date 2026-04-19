package com.nexora.core.user.entity;


import com.nexora.core.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class Roles extends BaseEntity {
    @Column(name = "name",nullable = false)
    private String name;


}
