package com.utp.clinitech.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "pacientes")
public class Paciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 8)
  private String dni;
  @Column(nullable = false, length = 100)
  private String nombres;
  @Column(nullable = false, length = 100)
  private String apellidos;
  @Column(name = "fecha_nacimiento", nullable = false)
  private LocalDate fechaNacimiento;
  @Column(nullable = false, length = 9)
  private String telefono;
  @Column(nullable = false, unique = true, length = 100)
  private String correo;
  @Column(length = 255)
  private String direccion;
  @Column(length = 500)
  private String alergias;
  @Column(name = "historial_medico", length = 1000)
  private String historialMedico;
  @Column(nullable = false)
  private boolean activo = true;
  @Column(name = "creado_en", nullable = false, updatable = false)
  private OffsetDateTime creadoEn;

  public Paciente() {
  }

  @PrePersist
  void prePersist() {
    creadoEn = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getDni() {
    return dni;
  }

  public String getNombres() {
    return nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public String getDireccion() {
    return direccion;
  }

  public String getAlergias() {
    return alergias;
  }

  public String getHistorialMedico() {
    return historialMedico;
  }

  public boolean isActivo() {
    return activo;
  }

  public void actualizar(String dni, String nombres, String apellidos, LocalDate fechaNacimiento, String telefono,
      String correo, String direccion, String alergias, String historialMedico) {
    this.dni = dni;
    this.nombres = nombres;
    this.apellidos = apellidos;
    this.fechaNacimiento = fechaNacimiento;
    this.telefono = telefono;
    this.correo = correo;
    this.direccion = direccion;
    this.alergias = alergias;
    this.historialMedico = historialMedico;
  }

  public void cambiarEstado(boolean activo) {
    this.activo = activo;
  }

  public String nombreCompleto() {
    return nombres + " " + apellidos;
  }
}
