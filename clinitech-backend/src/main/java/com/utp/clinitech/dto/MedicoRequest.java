package com.utp.clinitech.dto;

import jakarta.validation.constraints.Email; // Usado para validar el formato del correo electrónico del médico.
import jakarta.validation.constraints.NotBlank; // Usado para asegurar que los datos obligatorios no estén en blanco.
import jakarta.validation.constraints.NotNull; // Usado para validar que la especialidad médica asignada sea requerida.
import jakarta.validation.constraints.Pattern; // Usado para validar el formato de número CMP y número telefónico.
import jakarta.validation.constraints.Size; // Usado para limitar el tamaño de caracteres de nombres y horarios.

// DTO para recibir los datos de alta o modificación de un profesional médico.
public record MedicoRequest(
    @NotBlank @Size(max = 100) String nombres, 
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{5,10}") String cmp, 
    @NotNull Long especialidadId,
    @NotBlank @Pattern(regexp = "\\d{9}") String telefono, 
    @NotBlank @Email @Size(max = 100) String correo,
    @NotBlank @Size(max = 100) String horarioAtencion) { 
}
