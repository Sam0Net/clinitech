package com.utp.clinitech.model;

import java.time.OffsetDateTime; // Manejar fechas y horas con zona horaria.
import com.utp.clinitech.model.enums.EstadoCita; // Enum para representar el estado de la cita.
import com.utp.clinitech.model.enums.PrioridadCita; // Enum para representar la prioridad de la cita.
import jakarta.persistence.Column; // Anotación para mapear campos de la entidad a columnas de la base de datos.
import jakarta.persistence.Entity; // Anotación para indicar que esta clase es una entidad JPA.
import jakarta.persistence.EnumType; // Enum para especificar cómo se deben almacenar los valores de enumeración en la base de datos.
import jakarta.persistence.Enumerated; // Anotación para indicar que un campo es de tipo enumeración.
import jakarta.persistence.FetchType; // Enum para especificar cómo se deben cargar las relaciones entre entidades (perezoso o ansioso).
import jakarta.persistence.GeneratedValue; // Anotación para indicar que el valor de un campo se generará automáticamente.
import jakarta.persistence.GenerationType; // Enum para especificar la estrategia de generación de valores para un campo.
import jakarta.persistence.Id; // Anotación para indicar que un campo es la clave primaria de la entidad.
import jakarta.persistence.JoinColumn; // Anotación para especificar la columna que se utilizará para unir dos entidades en una relación.
import jakarta.persistence.ManyToOne; // Anotación para indicar que una entidad tiene una relación de muchos a uno con otra entidad.
import jakarta.persistence.PrePersist; // Anotación para indicar que un método debe ejecutarse antes de que la entidad se persista en la bd.
import jakarta.persistence.Table; // Anotación para especificar el nombre de la tabla en la base de datos que se mapeará a esta entidad.
import jakarta.persistence.UniqueConstraint; // Anotación para especificar restricciones de unicidad en la tabla de la base de datos.

@Entity // Indica que esta clase es una entidad JPA y se mapeará a una tabla en la bd.
@Table(name = "citas", uniqueConstraints = @UniqueConstraint(name = "uk_cita_medico_fecha", columnNames = { "medico_id",
    "fecha_hora" })) // Especifica el nombre de la tabla en la bd y define una restricción de unicidad para evitar que un médico tenga dos citas a la misma hora.
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
