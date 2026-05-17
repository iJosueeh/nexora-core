-- Migración para el Sistema de Seguidores (Followers)

CREATE TABLE IF NOT EXISTS seguidores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_following FOREIGN KEY (following_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT unique_follower_following UNIQUE (follower_id, following_id)
);

-- Índices para optimizar búsquedas de seguidores y seguidos
CREATE INDEX IF NOT EXISTS idx_seguidores_follower ON seguidores(follower_id);
CREATE INDEX IF NOT EXISTS idx_seguidores_following ON seguidores(following_id);

-- Asegurar que los contadores existen en la tabla perfiles
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfiles' AND column_name = 'followers_count') THEN
        ALTER TABLE perfiles ADD COLUMN followers_count INTEGER DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'perfiles' AND column_name = 'following_count') THEN
        ALTER TABLE perfiles ADD COLUMN following_count INTEGER DEFAULT 0;
    END IF;
END $$;
