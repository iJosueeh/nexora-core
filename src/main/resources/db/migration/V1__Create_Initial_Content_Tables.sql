-- Migración Inicial para Investigaciones y Eventos

-- Tabla de Investigaciones
CREATE TABLE research_papers (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    faculty VARCHAR(100) NOT NULL,
    views INTEGER DEFAULT 0,
    author_id UUID NOT NULL,
    pdf_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_research_author FOREIGN KEY (author_id) REFERENCES usuarios(id)
);

-- Tabla de Eventos
CREATE TABLE university_events (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    event_date VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    organizer_name VARCHAR(255),
    organizer_role VARCHAR(255),
    whatsapp VARCHAR(255),
    telegram VARCHAR(255),
    discord VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Intermedia para RSVPs (Asistentes a Eventos)
CREATE TABLE event_attendees (
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES university_events(id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para optimizar búsquedas por slug
CREATE INDEX idx_research_slug ON research_papers(slug);
CREATE INDEX idx_event_slug ON university_events(slug);
