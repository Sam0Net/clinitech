package com.utp.clinitech.model;

import java.time.OffsetDateTime;
import com.utp.clinitech.model.enums.EstadoCita;
import com.utp.clinitech.model.enums.PrioridadCita;
import jakarta.persistence.*;

@Entity
@Table(name = "citas", uniqueConstraints = @UniqueConstraint(name = "uk_cita_medico_fecha", columnNames = { "medico_id",
    "fecha_hora" }))
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "paciente_id")
  private Paciente paciente;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "medico_id")
  private Medico medico;
  @Column(name = "fecha_hora", nullable = false)
  private OffsetDateTime fechaHora;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoCita estado = EstadoCita.PENDIENTE;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PrioridadCita prioridad = PrioridadCita.NORMAL;
  @Column(name = "motivo_consulta", nullable = false, length = 255)
  private String motivoConsulta;
  @Column(name = "creado_en", nullable = false, updatable = false)
  private OffsetDateTime creadoEn;

  protected Cita() {
  }

  public Cita(Paciente paciente, Medico medico, OffsetDateTime fechaHora, PrioridadCita prioridad,
      String motivoConsulta) {
    this.paciente = paciente;
    this.medico = medico;
    this.fechaHora = fechaHora;
    this.prioridad = prioridad;
    this.motivoConsulta = motivoConsulta;
  }

  @PrePersist
  void prePersist() {
    creadoEn = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Paciente getPaciente() {
    return paciente;
  }

  public Medico getMedico() {
    return medico;
  }

  public OffsetDateTime getFechaHora() {
    return fechaHora;
  }

  public EstadoCita getEstado() {
    return estado;
  }

  public PrioridadCita getPrioridad() {
    return prioridad;
  }

  public String getMotivoConsulta() {
    return motivoConsulta;
  }

  public void reprogramar(OffsetDateTime fechaHora) {
    this.fechaHora = fechaHora;
    this.estado = EstadoCita.REPROGRAMADA;
  }

  public void cambiarEstado(EstadoCita estado) {
    this.estado = estado;
  }
}
