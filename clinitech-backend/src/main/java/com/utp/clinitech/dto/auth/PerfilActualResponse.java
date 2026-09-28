package com.utp.clinitech.dto.auth;
import com.utp.clinitech.model.enums.RolUsuario;
public record PerfilActualResponse(Long id, String username, RolUsuario rol, Long pacienteId, Long medicoId) { }
