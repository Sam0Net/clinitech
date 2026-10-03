package com.utp.clinitech.dto.auth;

import com.utp.clinitech.model.enums.RolUsuario;

public record LoginResponse(String accessToken, String tokenType, long expiresInMinutes, Long userId, String username,
    RolUsuario role) {
}
