package com.nexora.core.profile.entity;

import com.nexora.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="carreras")
public class Courses extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="facultad_id",nullable = false)
    public Faculties facultad;

    @Column(name="name", nullable = false)
    public String name;
}
