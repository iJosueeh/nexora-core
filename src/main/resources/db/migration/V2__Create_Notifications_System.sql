-- Migración para el Sistema de Notificaciones corregida

-- 1. Asegurar que existe la tabla de likes
CREATE TABLE IF NOT EXISTS post_likes (
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 2. Crear tabla de notificaciones sin el constraint problemático inicialmente
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    post_id UUID,
    event_id UUID,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- 3. Intentar añadir el constraint de eventos solo si la tabla existe
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'university_events') THEN
        ALTER TABLE notifications 
        ADD CONSTRAINT fk_notification_event 
        FOREIGN KEY (event_id) REFERENCES university_events(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 4. Índice para paginación
CREATE INDEX IF NOT EXISTS idx_notifications_user_pagination ON notifications(user_id, created_at DESC);

-- 5. Triggers (usando CREATE OR REPLACE para evitar errores si ya existen)
CREATE OR REPLACE FUNCTION notify_on_comment()
RETURNS TRIGGER AS $$
DECLARE
    post_owner_id UUID;
BEGIN
    SELECT autor_id INTO post_owner_id FROM posts WHERE id = NEW.post_id;
    IF post_owner_id != NEW.autor_id THEN
        INSERT INTO notifications (user_id, sender_id, type, post_id, content)
        VALUES (post_owner_id, NEW.autor_id, 'COMMENT', NEW.post_id, left(NEW.content, 100));
    END IF;
    IF NEW.parent_id IS NOT NULL THEN
        DECLARE
            parent_owner_id UUID;
        BEGIN
            SELECT autor_id INTO parent_owner_id FROM comentarios WHERE id = NEW.parent_id;
            IF parent_owner_id != NEW.autor_id AND parent_owner_id != post_owner_id THEN
                INSERT INTO notifications (user_id, sender_id, type, post_id, content)
                VALUES (parent_owner_id, NEW.autor_id, 'COMMENT_REPLY', NEW.post_id, left(NEW.content, 100));
            END IF;
        END;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notify_on_comment ON comentarios;
CREATE TRIGGER tr_notify_on_comment
AFTER INSERT ON comentarios
FOR EACH ROW EXECUTE FUNCTION notify_on_comment();

CREATE OR REPLACE FUNCTION notify_on_like()
RETURNS TRIGGER AS $$
DECLARE
    post_owner_id UUID;
BEGIN
    SELECT autor_id INTO post_owner_id FROM posts WHERE id = NEW.post_id;
    IF post_owner_id != NEW.user_id THEN
        INSERT INTO notifications (user_id, sender_id, type, post_id)
        VALUES (post_owner_id, NEW.user_id, 'LIKE', NEW.post_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notify_on_like ON post_likes;
CREATE TRIGGER tr_notify_on_like
AFTER INSERT ON post_likes
FOR EACH ROW EXECUTE FUNCTION notify_on_like();
