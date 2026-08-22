package com.utp.clinitech.model.enums;

public enum EstadoCita {
    PENDIENTE("Pendiente"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    REPROGRAMADA("Reprogramada");

    private final String descripcion;

    EstadoCita(String descripcion){
        this.descripcion = descripcion;
    }
    public String getDescripcion(){
        return descripcion;
    }
}
