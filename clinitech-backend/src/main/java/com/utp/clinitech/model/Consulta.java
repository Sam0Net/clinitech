package com.utp.clinitech.model;
//Imports
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
public class Consulta {
    //Atributos
    private Long id;

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long citaID;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 1000, message = "El diagnóstico no puede exceder los 1000 caracteres")
    private String diagnostico;

    @NotBlank(message = "El tratamiento es obligatorio")
    @Size(max = 1000, message = "El tratamiento no puede exceder los 1000 caracteres")
    private String tratamiento;

    @Size(max = 500, message = "Las observaciones no pueden exceder los 500 caracteres")
    private String observaciones;

    private LocalDateTime fechaAtencion;
    //Constructores
    //Vacio
    public Consulta(){
        this.fechaAtencion = LocalDateTime.now();
    }
    //Completo
    public Consulta(long id, Long citaID, String diagnostico, String tratamiento, String observaciones, LocalDateTime fechaAtencion) {
        this.id = id;
        this.citaID = citaID;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
        this.fechaAtencion = fechaAtencion;
    }
    //Registro rapido
    public Consulta(Long citaID, String diagnostico, String tratamiento){
        this.citaID = citaID;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fechaAtencion = LocalDateTime.now();
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getCitaID() {
        return citaID;
    }

    public void setCitaID(Long citaID) {
        this.citaID = citaID;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    // Metodos Auxiliares
    // Consulta con observaciones adicionales
    public boolean tieneObservaciones() {
        return this.observaciones != null && !this.observaciones.trim().isEmpty();
    }
    @Override
    public String toString() {
        return "Consulta{" +
                "id=" + id +
                ", citaId=" + citaID +
                ", fechaAtencion=" + fechaAtencion +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consulta consulta = (Consulta) o;
        return id != null && id.equals(consulta.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
