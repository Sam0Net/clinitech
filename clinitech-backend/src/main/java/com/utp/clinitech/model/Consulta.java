package com.utp.clinitech.model;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "consultas")
public class Consulta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cita_id", unique = true)
  private Cita cita;
  @Column(nullable = false, length = 1000)
  private String diagnostico;
  @Column(nullable = false, length = 1000)
  private String tratamiento;
  @Column(length = 500)
  private String observaciones;
  @Column(name = "fecha_atencion", nullable = false)
  private OffsetDateTime fechaAtencion;

  protected Consulta() {
  }

  public Consulta(Cita cita, String diagnostico, String tratamiento, String observaciones) {
    this.cita = cita;
    this.diagnostico = diagnostico;
    this.tratamiento = tratamiento;
    this.observaciones = observaciones;
  }

  @PrePersist
  void prePersist() {
    fechaAtencion = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Cita getCita() {
    return cita;
  }

  public String getDiagnostico() {
    return diagnostico;
  }

  public String getTratamiento() {
    return tratamiento;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public OffsetDateTime getFechaAtencion() {
    return fechaAtencion;
  }
}
