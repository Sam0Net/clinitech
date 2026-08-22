package com.utp.clinitech.model.enums;

public enum PrioridadCita {
    NORMAL("Normal"),
    URGENTE("Urgente");

    private final String descripcion;

    PrioridadCita(String descripcion){
        this.descripcion = descripcion;
    }
    public String getDescripcion(){
        return descripcion;
    }
}
