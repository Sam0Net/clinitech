package com.utp.clinitech.dto;
public record MedicoResponse(Long id, String nombres, String apellidos, String nombreCompleto, String cmp, EspecialidadResponse especialidad,
  String telefono, String correo, String horarioAtencion, boolean activo) { }
