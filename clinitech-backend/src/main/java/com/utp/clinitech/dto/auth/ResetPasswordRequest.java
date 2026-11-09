package com.utp.clinitech.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO para el reseteo administrativo de contraseñas.
public record ResetPasswordRequest(
    @NotBlank @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra mayúscula, una minúscula y un número") String password) {
}
