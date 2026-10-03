package com.utp.clinitech.dto;

// DTO inmutable que transfiere los datos profesionales y públicos de un médico al cliente.
public record MedicoResponse(
    Long id, 
    String nombres, 
    String apellidos, 
    String nombreCompleto, 
    String cmp, 
    EspecialidadResponse especialidad,
    String telefono, 
    String correo, 
    String horarioAtencion, 
    boolean activo) { 
}
