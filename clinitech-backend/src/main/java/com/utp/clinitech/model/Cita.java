package com.utp.clinitech.model;
// Imports
import com.utp.clinitech.model.enums.EstadoCita;
import com.utp.clinitech.model.enums.PrioridadCita;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
public class Cita {
    //Atributos
    private Long Id;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteID;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long medicoID;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    private LocalDateTime fechaHora;

    @NotNull(message = "El estado de la cita es obligatorio")
    private EstadoCita estado;

    @NotNull(message = "La prioridad de la cita es obligatoria")
    private PrioridadCita prioridad;

    @Size(max = 255, message = "El motivo de la consulta no puede exceder los 255 caracteres")
    private String motivoConsulta;

    private LocalDateTime fechaRegistro;
    //Constructores
    // Vacio
    public Cita(){
        this.estado = EstadoCita.PENDIENTE;
        this.prioridad = PrioridadCita.NORMAL;
        this.fechaRegistro = LocalDateTime.now();
    }
    // Completo
    public Cita(Long id, Long pacienteID, Long medicoID, LocalDateTime fechaHora, EstadoCita estado, PrioridadCita prioridad, String motivoConsulta, LocalDateTime fechaRegistro) {
        Id = id;
        this.pacienteID = pacienteID;
        this.medicoID = medicoID;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.prioridad = prioridad;
        this.motivoConsulta = motivoConsulta;
        this.fechaRegistro = fechaRegistro;
    }
    // Registro rápido
    public Cita(Long pacienteId, Long medicoId, LocalDateTime fechaHora,
                PrioridadCita prioridad, String motivoConsulta) {
        this.pacienteID = pacienteId;
        this.medicoID = medicoId;
        this.fechaHora = fechaHora;
        this.estado = EstadoCita.PENDIENTE;
        this.prioridad = prioridad;
        this.motivoConsulta = motivoConsulta;
        this.fechaRegistro = LocalDateTime.now();
    }
    // Getters y Setters

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getPacienteID() {
        return pacienteID;
    }

    public void setPacienteID(Long pacienteID) {
        this.pacienteID = pacienteID;
    }

    public Long getMedicoID() {
        return medicoID;
    }

    public void setMedicoID(Long medicoID) {
        this.medicoID = medicoID;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public PrioridadCita getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadCita prioridad) {
        this.prioridad = prioridad;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Metodos Auxiliares
    // Verifica urgencia
    public boolean esUrgencia() {
        return this.prioridad == PrioridadCita.URGENTE;
    }
    // Cita atendida
    public boolean estaAtendida() {
        return this.estado == EstadoCita.ATENDIDA;
    }
    @Override
    public String toString() {
        return "Cita{" +
                "id=" + Id +
                ", pacienteId=" + pacienteID +
                ", medicoId=" + medicoID +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                ", prioridad=" + prioridad +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cita cita = (Cita) o;
        return Id != null && Id.equals(cita.Id);
    }

    @Override
    public int hashCode() {
        return Id != null ? Id.hashCode() : 0;
    }

}
