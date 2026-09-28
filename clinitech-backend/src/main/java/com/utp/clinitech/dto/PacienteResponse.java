package com.utp.clinitech.dto;

import java.time.LocalDate;
public record PacienteResponse(Long id, String dni, String nombres, String apellidos, LocalDate fechaNacimiento, String telefono,
  String correo, String direccion, String alergias, String historialMedico, boolean activo) { }
