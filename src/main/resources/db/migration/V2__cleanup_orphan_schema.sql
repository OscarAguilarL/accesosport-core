-- Columnas removidas de LocationEmbeddable pero nunca eliminadas de la BD
ALTER TABLE events
    DROP COLUMN IF EXISTS latitude;
ALTER TABLE events
    DROP COLUMN IF EXISTS longitude;

-- Tabla de diseño anterior; la capacidad ahora vive en event_modalities
DROP TABLE IF EXISTS event_capacity;
