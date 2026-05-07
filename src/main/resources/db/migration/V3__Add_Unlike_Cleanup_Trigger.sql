-- Migración para limpiar notificaciones al deshacer interacciones

-- 1. Función para limpiar notificaciones cuando se elimina un Like
CREATE OR REPLACE FUNCTION clean_notification_on_unlike()
RETURNS TRIGGER AS $$
BEGIN
    -- Eliminar la notificación de tipo 'LIKE' generada por este usuario en este post
    DELETE FROM notifications 
    WHERE post_id = OLD.post_id 
      AND sender_id = OLD.user_id 
      AND type = 'LIKE';
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 2. Trigger asociado a la tabla post_likes
DROP TRIGGER IF EXISTS tr_clean_notification_on_unlike ON post_likes;
CREATE TRIGGER tr_clean_notification_on_unlike
AFTER DELETE ON post_likes
FOR EACH ROW EXECUTE FUNCTION clean_notification_on_unlike();

-- 3. (Opcional) Limpieza similar para comentarios si se eliminan físicamente
-- Nota: Si tu sistema usa borrado lógico para comentarios, este trigger no sería necesario
CREATE OR REPLACE FUNCTION clean_notification_on_delete_comment()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM notifications 
    WHERE post_id = OLD.post_id 
      AND sender_id = OLD.autor_id 
      AND type IN ('COMMENT', 'COMMENT_REPLY');
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_clean_notification_on_delete_comment ON comentarios;
CREATE TRIGGER tr_clean_notification_on_delete_comment
AFTER DELETE ON comentarios
FOR EACH ROW EXECUTE FUNCTION clean_notification_on_delete_comment();
