package com.utp.clinitech.dto.auth;

import com.utp.clinitech.model.enums.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(@NotBlank @Size(min = 4, max = 50) String username,
                                  @NotBlank @Size(min = 12, max = 128) String password,
                                  @NotNull RolUsuario rol, Long pacienteId, Long medicoId) { }
