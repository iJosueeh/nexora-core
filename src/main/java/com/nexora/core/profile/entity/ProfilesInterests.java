package com.nexora.core.profile.entity;

import com.nexora.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="perfiles_intereses")
public class ProfilesInterests extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="perfil_id")
    private Profiles profile;

    @ManyToOne
    @JoinColumn(name="interes_id")
    private AcademicInterests interes;


}
