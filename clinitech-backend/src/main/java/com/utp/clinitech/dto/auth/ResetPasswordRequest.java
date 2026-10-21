package com.utp.clinitech.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO para el reseteo administrativo de contraseñas.
public record ResetPasswordRequest(
    @NotBlank @Size(min = 6, max = 128) String password) {
}
