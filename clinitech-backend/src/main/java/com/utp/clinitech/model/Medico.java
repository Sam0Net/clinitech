package com.utp.clinitech.model;
//Imports
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class Medico {
    //Atributos privados
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
    private String apellidos;
    // Colegio Medico del Peru
    @NotBlank(message = "El CMP es obligatorio")
    @Size(min = 5, max = 10, message = "El CMP debe tener entre 5 y 10 caracteres")
    private String cmp;

    @NotNull(message = "Especialidad Obligatoria")
    private Long especialidadId;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe contener exactamente 9 dígitos")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "EL correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String correo;

    @Size(max = 100, message = "El horario de atención no puede exceder los 100 caracteres.")
    private String horarioAtencion; // Ej: "Lunes-Viernes 8:00-14:00"

    private LocalDateTime fechaRegistro;

    private Boolean estado;

    //Constructores
    // Vacio
    public Medico() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = true;
    }
    // Completo
    public Medico(Long id, String nombres, String apellidos, String cmp, Long especialidadId, String telefono, String correo, String horarioAtencion, LocalDateTime fechaRegistro, Boolean estado) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cmp = cmp;
        this.especialidadId = especialidadId;
        this.telefono = telefono;
        this.correo = correo;
        this.horarioAtencion = horarioAtencion;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }
    // Registro rápido
    public Medico(String cmp, String nombres, String apellidos, Long especialidadId, String telefono, String correo) {
        this.cmp = cmp;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidadId = especialidadId;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getHorarioAtencion() {
        return horarioAtencion;
    }

    public void setHorarioAtencion(String horarioAtencion) {
        this.horarioAtencion = horarioAtencion;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    // Metodos Auxiliares
    // Nombre Completo del Médico
    public String getNombreCompleto() {
        return this.nombres + " " + this.apellidos;
    }
    // Nombres + Especialidad
    public String getNombreTitulo(){
        return "Dr. " + getNombreCompleto();
    }
    @Override
    public String toString() {
        return "Medico{" +
                "id=" + id +
                ", cmp='" + cmp + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", especialidadId=" + especialidadId +
                ", correo='" + correo + '\'' +
                ", estado=" + estado +
                '}';
    }

    // Por investigar qué hace
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medico medico = (Medico) o;
        return id != null && id.equals(medico.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
