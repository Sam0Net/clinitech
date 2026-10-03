package com.utp.clinitech.dto;

import java.time.LocalDate; // Usado para transferir la fecha de nacimiento del paciente en formato estándar.

// DTO inmutable que devuelve la ficha completa de un paciente para visualización o consultas.
public record PacienteResponse(
    Long id, 
    String dni, 
    String nombres, 
    String apellidos, 
    LocalDate fechaNacimiento, 
    String telefono,
    String correo, 
    String direccion, 
    String alergias, 
    String historialMedico, 
    boolean activo) { 
}
