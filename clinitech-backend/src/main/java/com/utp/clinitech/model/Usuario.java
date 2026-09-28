package com.utp.clinitech.model;

import java.time.OffsetDateTime;
import com.utp.clinitech.model.enums.RolUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
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

  public Usuario(String username, String passwordHash, RolUsuario rol, Paciente paciente, Medico medico) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.rol = rol;
    this.paciente = paciente;
    this.medico = medico;
  }

  @PrePersist
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
