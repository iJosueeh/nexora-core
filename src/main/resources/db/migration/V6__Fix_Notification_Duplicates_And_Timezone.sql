-- Migración para corregir duplicados y zona horaria de notificaciones

-- 1. Ajustar la zona horaria por defecto de la base de datos a Lima (opcional pero recomendado)
-- SET TIME ZONE 'America/Lima';

-- 2. Mejorar el trigger de seguimiento para evitar duplicados y limpiar historial previo
CREATE OR REPLACE FUNCTION notify_on_follow()
RETURNS TRIGGER AS $$
BEGIN
    -- Eliminar notificaciones de seguimiento previas del mismo usuario para evitar spam/duplicados
    DELETE FROM notifications 
    WHERE user_id = NEW.following_id 
      AND sender_id = NEW.follower_id 
      AND type = 'FOLLOW';

    -- Insertar la nueva notificación con timestamp explícito en hora de Perú (UTC-5)
    INSERT INTO notifications (user_id, sender_id, type, content, created_at, updated_at)
    VALUES (
        NEW.following_id, 
        NEW.follower_id, 
        'FOLLOW', 
        'ha comenzado a seguirte',
        CURRENT_TIMESTAMP AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima',
        CURRENT_TIMESTAMP AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima'
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Asegurar que los registros existentes y futuros de la tabla usen la zona horaria correcta
ALTER TABLE notifications ALTER COLUMN created_at SET DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima');
ALTER TABLE notifications ALTER COLUMN updated_at SET DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima');

-- 4. Limpiar duplicados actuales para que el usuario vea su lista limpia inmediatamente
DELETE FROM notifications n1
USING notifications n2
WHERE n1.id < n2.id
  AND n1.user_id = n2.user_id
  AND n1.sender_id = n2.sender_id
  AND n1.type = 'FOLLOW';
