package com.utp.clinitech.dto.auth;

import com.utp.clinitech.model.enums.RolUsuario; // Usado para retornar el rol del usuario autenticado en la respuesta de inicio de sesión.

// DTO inmutable que encapsula el token de acceso JWT y los datos de identidad tras una autenticación exitosa.
public record LoginResponse(
    String accessToken, 
    String tokenType, 
    long expiresInMinutes, 
    Long userId, 
    String username,
    RolUsuario role) {
}
