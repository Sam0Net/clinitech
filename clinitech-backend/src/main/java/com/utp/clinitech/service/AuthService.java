package com.utp.clinitech.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.MedicoDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.dto.auth.CrearUsuarioRequest;
import com.utp.clinitech.dto.auth.LoginRequest;
import com.utp.clinitech.dto.auth.LoginResponse;
import com.utp.clinitech.dto.auth.PerfilActualResponse;
import com.utp.clinitech.dto.auth.RegistroPacienteRequest;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Medico;
import com.utp.clinitech.model.Paciente;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;
import com.utp.clinitech.security.JwtService;

@Service
@Transactional(readOnly = true)
public class AuthService {
  private final UsuarioDAO usuarios; private final PacienteDAO pacientes; private final MedicoDAO medicos;
  private final PasswordEncoder passwordEncoder; private final JwtService jwtService; private final CurrentUserService currentUser;
  private final PacienteService pacienteService;

  public AuthService(UsuarioDAO usuarios, PacienteDAO pacientes, MedicoDAO medicos, PasswordEncoder passwordEncoder, JwtService jwtService, CurrentUserService currentUser, PacienteService pacienteService) {
    this.usuarios = usuarios; this.pacientes = pacientes; this.medicos = medicos; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService; this.currentUser = currentUser; this.pacienteService = pacienteService;
  }
  @Transactional
  public LoginResponse login(LoginRequest request) {
    Usuario user = usuarios.findByUsernameIgnoreCase(request.username()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
    if (!user.isActivo() || user.getRol() != request.role() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    user.registrarAcceso();
    return new LoginResponse(jwtService.generar(user), "Bearer", jwtService.expirationMinutes(), user.getId(), user.getUsername(), user.getRol());
  }
  public PerfilActualResponse perfilActual() {
    Usuario user = currentUser.required();
    return new PerfilActualResponse(user.getId(), user.getUsername(), user.getRol(), user.getPaciente() == null ? null : user.getPaciente().getId(), user.getMedico() == null ? null : user.getMedico().getId());
  }
  @Transactional
  public Long crearUsuario(CrearUsuarioRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    if (usuarios.existsByUsernameIgnoreCase(request.username())) throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
    Paciente paciente = request.pacienteId() == null ? null : pacientes.findById(request.pacienteId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    Medico medico = request.medicoId() == null ? null : medicos.findById(request.medicoId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado"));
    return usuarios.save(new Usuario(request.username().trim(), passwordEncoder.encode(request.password()), request.rol(), paciente, medico)).getId();
  }

  @Transactional
  public LoginResponse registrarPaciente(RegistroPacienteRequest request) {
    if (usuarios.existsByUsernameIgnoreCase(request.username())) {
      throw new ApiException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado");
    }
    if (pacientes.findByDni(request.dni()).isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    }
    Paciente paciente = new Paciente();
    paciente.actualizar(
        request.dni().trim(),
        request.nombres().trim(),
        request.apellidos().trim(),
        request.fechaNacimiento(),
        request.telefono().trim(),
        request.correo().trim().toLowerCase(),
        request.direccion(),
        request.alergias(),
        request.historialMedico()
    );
    Paciente guardado = pacienteService.registrarNuevoPaciente(paciente);
    Usuario usuario = new Usuario(
        request.username().trim(),
        passwordEncoder.encode(request.password()),
        RolUsuario.PACIENTE,
        guardado,
        null
    );
    usuario.registrarAcceso();
    Usuario usuarioGuardado = usuarios.save(usuario);
    return new LoginResponse(
        jwtService.generar(usuarioGuardado),
        "Bearer",
        jwtService.expirationMinutes(),
        usuarioGuardado.getId(),
        usuarioGuardado.getUsername(),
        usuarioGuardado.getRol()
    );
  }
}
