package com.utp.clinitech.model;

import java.time.OffsetDateTime; // Manejar fechas y horas con zona horaria.
import com.utp.clinitech.model.enums.RolUsuario; // Rol del usuario permitidios (PACIENTE, MEDICO, ADMIN, RECEPCIONISTA).
import jakarta.persistence.Column; // Anotación para definir propiedades de columnas en la bd.
import jakarta.persistence.Entity; // Anotación para marcar la clase como una entidad JPA.
import jakarta.persistence.EnumType; // Anotación para definir cómo se almacenan los enums en la bd.
import jakarta.persistence.Enumerated; // Anotación para indicar que un campo es un enum y cómo se debe persistir.
import jakarta.persistence.FetchType; // Anotación para definir la estrategia de carga de relaciones (EAGER o LAZY).
import jakarta.persistence.GeneratedValue; // Anotación para indicar que el valor de la columna se genera automáticamente.
import jakarta.persistence.GenerationType; // Estrategia de generación de valores para la columna (IDENTITY, SEQUENCE, TABLE, AUTO).
import jakarta.persistence.Id; // Anotación para marcar un campo como la clave primaria de la entidad.
import jakarta.persistence.JoinColumn; // Anotación para definir la columna de unión en una relación entre entidades.
import jakarta.persistence.OneToOne; // Anotación para definir una relación uno a uno entre entidades.
import jakarta.persistence.PrePersist; // Anotación para definir un método que se ejecuta antes de persistir la entidad.
import jakarta.persistence.Table; // Anotación para definir el nombre de la tabla en la bd para la entidad.

@Entity // Marca la clase como una entidad JPA que se mapeará a una tabla en la bd.
@Table(name = "usuarios") // Define el nombre de la tabla en la base de datos para esta entidad.
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 50)
  private String username;
  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RolUsuario rol;
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "paciente_id", unique = true)
  private Paciente paciente;
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medico_id", unique = true)
  private Medico medico;
  @Column(nullable = false)
  private boolean activo = true;
  @Column(name = "creado_en", nullable = false, updatable = false)
  private OffsetDateTime creadoEn;
  @Column(name = "ultimo_acceso")
  private OffsetDateTime ultimoAcceso;

  protected Usuario() {
  }

  // Constructor para crear un usuario con los campos obligatorios.
  public Usuario(String username, String passwordHash, RolUsuario rol, Paciente paciente, Medico medico) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.rol = rol;
    this.paciente = paciente;
    this.medico = medico;
  }

  @PrePersist
  // Método que se ejecuta antes de persistir la entidad en la base de datos.
  void prePersist() {
    creadoEn = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public RolUsuario getRol() {
    return rol;
  }

  public Paciente getPaciente() {
    return paciente;
  }

  public Medico getMedico() {
    return medico;
  }

  public boolean isActivo() {
    return activo;
  }

  public void registrarAcceso() {
    ultimoAcceso = OffsetDateTime.now();
  }

  public void cambiarEstado(boolean activo) {
    this.activo = activo;
  }
}
