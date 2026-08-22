package com.utp.clinitech.model.enums;

public enum RolUsuario {
    PACIENTE("Paciente"),
    MEDICO("Médico"),
    ADMIN("Administrador");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
