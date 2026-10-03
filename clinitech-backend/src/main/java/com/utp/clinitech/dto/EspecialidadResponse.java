package com.utp.clinitech.dto;

// DTO inmutable que devuelve la información de una especialidad médica y su estado de vigencia.
public record EspecialidadResponse(
    Long id, 
    String nombre, 
    String descripcion, 
    boolean activo) {
}
