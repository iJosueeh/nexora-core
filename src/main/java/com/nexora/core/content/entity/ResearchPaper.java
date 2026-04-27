package com.nexora.core.content.entity;

import java.util.UUID;
import com.nexora.core.common.entity.AuditableBaseEntity;
import com.nexora.core.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "research_papers")
public class ResearchPaper extends AuditableBaseEntity {

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "faculty", nullable = false)
    private String faculty;

    @Column(name = "views")
    private Integer views = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "pdf_url")
    private String pdfUrl;
}
