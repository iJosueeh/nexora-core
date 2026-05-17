-- Migración para añadir notificaciones automáticas al seguir usuarios

CREATE OR REPLACE FUNCTION notify_on_follow()
RETURNS TRIGGER AS $$
BEGIN
    -- Insertar notificación para el usuario seguido
    INSERT INTO notifications (user_id, sender_id, type, content)
    VALUES (NEW.following_id, NEW.follower_id, 'FOLLOW', 'ha comenzado a seguirte');
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Borrar trigger si existe y crearlo de nuevo
DROP TRIGGER IF EXISTS tr_notify_on_follow ON seguidores;
CREATE TRIGGER tr_notify_on_follow
AFTER INSERT ON seguidores
FOR EACH ROW EXECUTE FUNCTION notify_on_follow();

-- Asegurar que la tabla notifications tiene habilitado Realtime (para Supabase)
-- Nota: Esto depende de la configuración de la publicación de Supabase
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'supabase_realtime') THEN
        BEGIN
            ALTER PUBLICATION supabase_realtime ADD TABLE notifications;
        EXCEPTION WHEN others THEN
            -- Si ya está en la publicación o hay error de permisos, ignorar
        END;
    END IF;
END $$;
