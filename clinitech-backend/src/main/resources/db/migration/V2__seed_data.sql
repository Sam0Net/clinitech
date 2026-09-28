-- Migración de datos semilla iniciales para especialidades
INSERT INTO especialidades (nombre, descripcion, activo)
VALUES 
    ('Medicina General', 'Atención primaria y triaje médico', TRUE),
    ('Cardiología', 'Diagnóstico y tratamiento de afecciones cardíacas', TRUE),
    ('Pediatría', 'Atención médica integral infantil', TRUE),
    ('Dermatología', 'Cuidado y tratamiento de la piel', TRUE),
    ('Traumatología', 'Lesiones óseas y del sistema locomotor', TRUE)
ON CONFLICT (nombre) DO NOTHING;
