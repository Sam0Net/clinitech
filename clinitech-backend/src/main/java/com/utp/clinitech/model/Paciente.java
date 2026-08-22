package com.utp.clinitech.model;

// Imports
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Paciente {
  // Atributos
  private Long id;

  @NotBlank(message = "El DNI es obligatorio")
  @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
  @Pattern(regexp = "\\d{8}", message = "EL DNI debe contener solo números")
  private String dni;

  @NotBlank(message = "Los nombres son obligatorios")
  @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres")
  private String nombres;

  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
  private String apellidos;

  @NotNull(message = "La fecha de nacimiento es obligatoria")
  private LocalDate fechaNacimiento;

  @NotBlank(message = "El teléfono es obligatorio")
  @Pattern(regexp = "\\d{9}", message = "El teléfono debe contener exactamente 9 dígitos")
  private String telefono;

  @NotBlank(message = "El correo es obligatorio")
  @Email(message = "EL correo debe tener un formato válido")
  @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
  private String correo;

  @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
  private String direccion;

  @Size(max = 500, message = "Las alergias no pueden exceder los 500 caracteres")
  private String alergias;

  @Size(max = 1000, message = "El historial médico no puede exceder los 1000 caracteres")
  private String historialMedico;

  private LocalDateTime fechaRegistro;

  private Boolean estado; // true = activo, false = inactivo

  // Constructores
  // Vacio, lo requiere Spring JDBC para mapear los resultados de la base de datos
  public Paciente() {
    this.fechaRegistro = LocalDateTime.now();
    this.estado = true;
  }

  // Constructor con parámetros
  public Paciente(Long id, String dni, String nombres, String apellidos, LocalDate fechaNacimiento, String telefono,
      String correo, String direccion, String alergias, String historialMedico) {
    this.id = id;
    this.dni = dni;
    this.nombres = nombres;
    this.apellidos = apellidos;
    this.fechaNacimiento = fechaNacimiento;
    this.telefono = telefono;
    this.correo = correo;
    this.direccion = direccion;
    this.alergias = alergias;
    this.historialMedico = historialMedico;
    this.fechaRegistro = LocalDateTime.now();
    this.estado = true;
  }

  // Constructor para registro mínimo (campos mínimos obligatorios)
  public Paciente(String dni, String nombres, String apellidos, LocalDate fechaNacimiento, String telefono,
      String correo) {
    this.dni = dni;
    this.nombres = nombres;
    this.apellidos = apellidos;
    this.fechaNacimiento = fechaNacimiento;
    this.telefono = telefono;
    this.correo = correo;
    this.fechaRegistro = LocalDateTime.now();
    this.estado = true;
  }

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public void setFechaNacimiento(LocalDate fechaNacimiento) {
    this.fechaNacimiento = fechaNacimiento;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getAlergias() {
    return alergias;
  }

  public void setAlergias(String alergias) {
    this.alergias = alergias;
  }

  public String getHistorialMedico() {
    return historialMedico;
  }

  public void setHistorialMedico(String historialMedico) {
    this.historialMedico = historialMedico;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public Boolean getEstado() {
    return estado;
  }

  public void setEstado(Boolean estado) {
    this.estado = estado;
  }

  // Metodos auxiliares
  // Nombre completo del Paciente
  public String getNombreCompleto(){
    return this.nombres + " " + this.apellidos;
  }
  // Calcula edad de paciente
  public int calcularEdad() {
    if (this.fechaNacimiento == null) {
      return 0;
    }
    return LocalDate.now().getYear() - this.fechaNacimiento.getYear();
  }
  // Mayor de edad
  public boolean esMayorDeEdad() {
    return calcularEdad() >= 18;
  }
  @Override
  public String toString() {
    return "Paciente{" +
            "id=" + id +
            ", dni='" + dni + '\'' +
            ", nombres='" + nombres + '\'' +
            ", apellidos='" + apellidos + '\'' +
            ", fechaNacimiento=" + fechaNacimiento +
            ", telefono='" + telefono + '\'' +
            ", correo='" + correo + '\'' +
            ", estado=" + estado +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Paciente paciente = (Paciente) o;
    return id != null && id.equals(paciente.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

}