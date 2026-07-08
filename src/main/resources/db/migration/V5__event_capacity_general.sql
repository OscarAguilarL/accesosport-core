-- Recrear la tabla event_capacity (fue eliminada en V2)
CREATE TABLE event_capacity (
    event_id     UUID    NOT NULL,
    max_capacity INTEGER NOT NULL,
    reserved     INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT event_capacity_pkey PRIMARY KEY (event_id),
    CONSTRAINT event_capacity_event_fk FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Poblar con la suma de cupos actuales de las modalidades de cada evento.
INSERT INTO event_capacity (event_id, max_capacity, reserved)
SELECT
    em.event_id,
    SUM(em.capacity)                                                           AS max_capacity,
    COUNT(r.id) FILTER (WHERE r.status IN ('CONFIRMED', 'PENDING_PAYMENT'))    AS reserved
FROM event_modalities em
LEFT JOIN registrations r ON r.event_id = em.event_id
GROUP BY em.event_id;

-- Eliminar la columna capacity de event_modalities
ALTER TABLE event_modalities DROP COLUMN capacity;
