package com.nexora.core.content.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nexora.core.common.entity.AuditableBaseEntity;
import com.nexora.core.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "posts")
public class Post extends AuditableBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private User autor;

    @Column(name = "tipo_id")
    private UUID tipoId;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial = false;

    @Column(name = "status")
    private String status = "PUBLISHED";

    @Column(name = "location")
    private String location;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comentarios = new ArrayList<>();
}
