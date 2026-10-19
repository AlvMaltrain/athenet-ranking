-- Agrega la fecha del partido a resultado_procesado, para poder determinar
-- el "ultimo encuentro" de un equipo de forma confiable (antes solo se podia
-- aproximar por partido_id). Nullable: filas ya procesadas antes de este
-- cambio, o mensajes de Competencia que aun no envien la fecha, quedan en NULL.
ALTER TABLE resultado_procesado ADD COLUMN fecha_partido DATE;
