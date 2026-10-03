package com.utp.clinitech.model;

import java.time.OffsetDateTime; // Usado para registrar la fecha y hora de la atención médica con zona horaria.
import jakarta.persistence.Column; // Usado para mapear atributos a columnas específicas en la tabla de la base de datos.
import jakarta.persistence.Entity; // Usado para marcar la clase como una entidad persistente gestionada por JPA.
import jakarta.persistence.FetchType; // Usado para configurar la estrategia de carga diferida (LAZY) de la relación con la cita.
import jakarta.persistence.GeneratedValue; // Usado para configurar la generación automática del identificador único.
import jakarta.persistence.GenerationType; // Usado para indicar la estrategia de autoincremento (IDENTITY).
import jakarta.persistence.Id; // Usado para designar el campo de clave primaria de la entidad.
import jakarta.persistence.JoinColumn; // Usado para especificar la columna de clave foránea que enlaza con la cita.
import jakarta.persistence.OneToOne; // Usado para modelar una asociación uno a uno exclusiva entre la cita y su consulta.
import jakarta.persistence.PrePersist; // Usado para ejecutar lógica automática antes de persistir la entidad en base de datos.
import jakarta.persistence.Table; // Usado para vincular la entidad con la tabla correspondiente en la base de datos.

// Entidad que representa la atención médica realizada, registrando diagnóstico y receta clínica.
@Entity
@Table(name = "consultas")
public class Consulta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // Identificador único de la consulta clínica.

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cita_id", unique = true)
  private Cita cita; // Cita médica asociada que fue atendida.

  @Column(nullable = false, length = 1000)
  private String diagnostico; // Diagnóstico clínico determinado por el médico tratante.

  @Column(nullable = false, length = 1000)
  private String tratamiento; // Medicación, dosis o plan terapéutico recetado.

  @Column(length = 500)
  private String observaciones; // Observaciones adicionales o notas de seguimiento médico.

  @Column(name = "fecha_atencion", nullable = false)
  private OffsetDateTime fechaAtencion; // Marca temporal exacta en la que se concluyó la atención.

  protected Consulta() {
  }

  // Constructor para registrar una nueva consulta médica asociada a una cita.
  public Consulta(Cita cita, String diagnostico, String tratamiento, String observaciones) {
    this.cita = cita;
    this.diagnostico = diagnostico;
    this.tratamiento = tratamiento;
    this.observaciones = observaciones;
  }

  // Asigna automáticamente la fecha y hora de atención al momento de guardar.
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
