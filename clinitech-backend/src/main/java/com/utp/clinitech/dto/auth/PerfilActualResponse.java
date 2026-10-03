package com.utp.clinitech.dto.auth;

import com.utp.clinitech.model.enums.RolUsuario; // Usado para indicar el rol de seguridad del usuario autenticado en sesión.

// DTO inmutable que devuelve los datos del perfil y enlaces a paciente o médico para el usuario actual.
public record PerfilActualResponse(
    Long id, 
    String username, 
    RolUsuario rol, 
    Long pacienteId, 
    Long medicoId) {
}
