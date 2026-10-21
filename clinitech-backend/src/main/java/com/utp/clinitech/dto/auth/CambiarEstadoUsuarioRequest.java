package com.utp.clinitech.dto.auth;

// DTO para habilitar o inhabilitar el acceso de una cuenta de usuario.
public record CambiarEstadoUsuarioRequest(boolean activo) {
}
