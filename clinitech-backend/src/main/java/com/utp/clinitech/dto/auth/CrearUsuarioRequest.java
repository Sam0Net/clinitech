package com.utp.clinitech.dto.auth;

import com.utp.clinitech.model.enums.RolUsuario; // Usado para tipar el rol asignado al nuevo usuario en el sistema.
import jakarta.validation.constraints.NotBlank; // Usado para asegurar que username y password no sean cadenas vacías.
import jakarta.validation.constraints.NotNull; // Usado para garantizar que el rol sea obligatorio.
import jakarta.validation.constraints.Size; // Usado para validar las longitudes mínimas y máximas de credenciales.

// DTO para la creación administrativa de nuevas cuentas de usuario vinculadas a médicos o pacientes.
public record CrearUsuarioRequest(
    @NotBlank @Size(min = 4, max = 50) String username,
    @NotBlank @Size(min = 12, max = 128) String password,
    @NotNull RolUsuario rol, 
    Long pacienteId, 
    Long medicoId) {
}
