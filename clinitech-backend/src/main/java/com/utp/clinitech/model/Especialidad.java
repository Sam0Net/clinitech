package com.utp.clinitech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidades")
public class Especialidad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 100)
  private String nombre;
  @Column(length = 255)
  private String descripcion;
  @Column(nullable = false)
  private boolean activo = true;

  protected Especialidad() {
  }

  public Especialidad(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public boolean isActivo() {
    return activo;
  }

  public void actualizar(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public void cambiarEstado(boolean activo) {
    this.activo = activo;
  }
}
