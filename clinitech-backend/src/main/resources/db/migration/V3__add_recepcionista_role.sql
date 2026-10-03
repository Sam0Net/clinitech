-- Actualización de la restricción de vínculo de usuarios para soportar el rol RECEPCIONISTA
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS ck_usuario_vinculo;

ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_vinculo CHECK (
    (rol = 'ADMIN' AND paciente_id IS NULL AND medico_id IS NULL) OR
    (rol = 'RECEPCIONISTA' AND paciente_id IS NULL AND medico_id IS NULL) OR
    (rol = 'PACIENTE' AND paciente_id IS NOT NULL AND medico_id IS NULL) OR
    (rol = 'MEDICO' AND medico_id IS NOT NULL AND paciente_id IS NULL)
);
