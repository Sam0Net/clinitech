package com.utp.clinitech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicos")
public class Medico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 100)
  private String nombres;
  @Column(nullable = false, length = 100)
  private String apellidos;
  @Column(nullable = false, unique = true, length = 10)
  private String cmp;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "especialidad_id")
  private Especialidad especialidad;
  @Column(nullable = false, length = 9)
  private String telefono;
  @Column(nullable = false, unique = true, length = 100)
  private String correo;
  @Column(name = "horario_atencion", nullable = false, length = 100)
  private String horarioAtencion;
  @Column(nullable = false)
  private boolean activo = true;

  public Medico() {
  }

  public Long getId() {
    return id;
  }

  public String getNombres() {
    return nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public String getCmp() {
    return cmp;
  }

  public Especialidad getEspecialidad() {
    return especialidad;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public String getHorarioAtencion() {
    return horarioAtencion;
  }

  public boolean isActivo() {
    return activo;
  }

  public String nombreCompleto() {
    return "Dr. " + nombres + " " + apellidos;
  }

  public void actualizar(String nombres, String apellidos, String cmp, Especialidad especialidad, String telefono,
      String correo, String horarioAtencion) {
    this.nombres = nombres;
    this.apellidos = apellidos;
    this.cmp = cmp;
    this.especialidad = especialidad;
    this.telefono = telefono;
    this.correo = correo;
    this.horarioAtencion = horarioAtencion;
  }

  public void cambiarEstado(boolean activo) {
    this.activo = activo;
  }
}
