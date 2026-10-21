package com.utp.clinitech.dto.auth;

import java.time.OffsetDateTime;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

// DTO para enviar la información de una cuenta de usuario a la vista administrativa.
public record UsuarioResponse(
    Long id,
    String username,
    RolUsuario rol,
    boolean activo,
    OffsetDateTime creadoEn,
    OffsetDateTime ultimoAcceso,
    Long medicoId,
    String medicoNombre,
    Long pacienteId,
    String pacienteNombre) {

  public static UsuarioResponse from(Usuario u) {
    String medicoNombre = u.getMedico() == null
        ? null
        : (u.getMedico().getNombres() + " " + u.getMedico().getApellidos());
    String pacienteNombre = u.getPaciente() == null
        ? null
        : (u.getPaciente().getNombres() + " " + u.getPaciente().getApellidos());

    return new UsuarioResponse(
        u.getId(),
        u.getUsername(),
        u.getRol(),
        u.isActivo(),
        u.getCreadoEn(),
        u.getUltimoAcceso(),
        u.getMedico() == null ? null : u.getMedico().getId(),
        medicoNombre,
        u.getPaciente() == null ? null : u.getPaciente().getId(),
        pacienteNombre
    );
  }
}
