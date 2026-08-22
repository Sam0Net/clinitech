package com.utp.clinitech.model;

import com.utp.clinitech.model.enums.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class Usuario {
    //Atributos
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password; // Se almacenará encriptada con bcrypt

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    @NotNull(message = "El ID de persona es obligatorio")
    private Long personaId; // Puede ser pacienteId o medicoId

    private LocalDateTime fechaRegistro;

    private LocalDateTime ultimoAcceso;

    private Boolean estado; // true = activo, false = inactivo/bloqueado


    //Constructores
    //Vacio
    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = true;
    }
    //Completo
    public Usuario(Long id, String username, String password,
                   RolUsuario rol, Long personaId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.personaId = personaId;
        this.fechaRegistro = LocalDateTime.now();
        this.estado = true;
    }
    //Registro rápido
    public Usuario(String username, String password, RolUsuario rol, Long personaId) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.personaId = personaId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    //Métodos Auxiliares
    //Verifica si el usuario tiene rol de administrador
    public boolean esAdministrador() {
        return this.rol == RolUsuario.ADMIN;
    }
    //Verifica si el usuario tiene rol de médico
    public boolean esMedico() {
        return this.rol == RolUsuario.MEDICO;
    }
    //Verifica si el usuario tiene rol de paciente
    public boolean esPaciente() {
        return this.rol == RolUsuario.PACIENTE;
    }
    //Registra el último acceso al sistema
    public void registrarAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", personaId=" + personaId +
                ", estado=" + estado +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
