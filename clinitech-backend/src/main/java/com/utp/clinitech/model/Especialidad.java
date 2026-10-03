package com.utp.clinitech.model;

import jakarta.persistence.Column; // agregar anotaciones de columna a los campos de la entidad.
import jakarta.persistence.Entity; // marcar la clase como una entidad JPA.
import jakarta.persistence.GeneratedValue; // especificar la estrategia de generación de valores para la clave primaria.
import jakarta.persistence.GenerationType; // especificar el tipo de valor generado para la clave primaria.
import jakarta.persistence.Id; // marcar el campo como la clave primaria de la entidad.
import jakarta.persistence.Table; // especificar el nombre de la tabla en la base de datos para la entidad.

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
