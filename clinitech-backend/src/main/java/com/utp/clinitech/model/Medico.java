package com.utp.clinitech.model;

import jakarta.persistence.Column; // Anotación para mapear una columna de la base de datos.
import jakarta.persistence.Entity; // Anotación para indicar que esta clase es una entidad de JPA.
import jakarta.persistence.FetchType; // Enumeración para definir la estrategia de carga de datos (EAGER o LAZY).
import jakarta.persistence.GeneratedValue; // Anotación para indicar que el valor del campo será generado automáticamente.
import jakarta.persistence.GenerationType; // Enumeración para definir la estrategia de generación de valores (IDENTITY, SEQUENCE, TABLE, AUTO).
import jakarta.persistence.Id; // Anotación para indicar que este campo es la clave primaria de la entidad.
import jakarta.persistence.JoinColumn; // Anotación para definir la columna que se utilizará para la relación entre entidades.
import jakarta.persistence.ManyToOne; // Anotación para indicar una relación de muchos a uno entre entidades.
import jakarta.persistence.Table; // Anotación para mapear la entidad a una tabla específica de la base de datos.

@Entity // Indica que esta clase es una entidad de JPA y se mapeará a una tabla de la bd.
@Table(name = "medicos") // Especifica el nombre de la tabla en la base de datos a la que se mapeará esta entidad.
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
