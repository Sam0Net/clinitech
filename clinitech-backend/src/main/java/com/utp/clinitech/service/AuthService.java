package com.utp.clinitech.service;

import org.springframework.http.HttpStatus; // Usado para asociar códigos de estado HTTP a las excepciones de autenticación.
import org.springframework.security.crypto.password.PasswordEncoder; // Usado para validar y codificar contraseñas con hashing BCrypt.
import org.springframework.stereotype.Service; // Usado para registrar la clase como servicio de negocio en Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para ejecutar las operaciones de usuario en transacciones atómicas.
import com.utp.clinitech.dao.MedicoDAO; // Usado para vincular cuentas de usuario con registros médicos existentes.
import com.utp.clinitech.dao.PacienteDAO; // Usado para verificar y vincular cuentas de usuario con pacientes.
import com.utp.clinitech.dao.UsuarioDAO; // Usado para consultar y persistir las cuentas de usuario.
import com.utp.clinitech.dto.auth.CrearUsuarioRequest; // Usado para recibir datos al dar de alta usuarios administrativos.
import com.utp.clinitech.dto.auth.LoginRequest; // Usado para recibir las credenciales ingresadas por el usuario.
import com.utp.clinitech.dto.auth.LoginResponse; // Usado para devolver el token generado y detalles de la sesión.
import com.utp.clinitech.dto.auth.PerfilActualResponse; // Usado para devolver los datos de identidad del usuario en sesión.
import com.utp.clinitech.dto.auth.RegistroPacienteRequest; // Usado para recibir la solicitud de registro público de pacientes.
import com.utp.clinitech.exception.ApiException; // Usado para emitir errores de negocio controlados (ej. credenciales inválidas).
import com.utp.clinitech.model.Medico; // Usado para asociar el usuario a un médico cuando corresponda.
import com.utp.clinitech.model.Paciente; // Usado para asociar el usuario a un paciente en la base de datos.
import com.utp.clinitech.model.Usuario; // Usado para instanciar y persistir el usuario autenticado.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para validar el rol esperado en el inicio de sesión.
import com.utp.clinitech.security.JwtService; // Usado para emitir y firmar tokens JWT válidos.

// Servicio responsable de los flujos de autenticación, emisión de tokens y registro de usuarios.
@Service
@Transactional(readOnly = true)
public class AuthService {
  private final UsuarioDAO usuarios; 
  private final PacienteDAO pacientes; 
  private final MedicoDAO medicos;
  private final PasswordEncoder passwordEncoder; 
  private final JwtService jwtService; 
  private final CurrentUserService currentUser;
  private final PacienteService pacienteService;

  public AuthService(UsuarioDAO usuarios, PacienteDAO pacientes, MedicoDAO medicos, PasswordEncoder passwordEncoder, JwtService jwtService, CurrentUserService currentUser, PacienteService pacienteService) {
    this.usuarios = usuarios; 
    this.pacientes = pacientes; 
    this.medicos = medicos; 
    this.passwordEncoder = passwordEncoder; 
    this.jwtService = jwtService; 
    this.currentUser = currentUser; 
    this.pacienteService = pacienteService;
  }

  // Autentica credenciales de usuario, verifica rol solicitado y emite un token JWT.
  @Transactional
  public LoginResponse login(LoginRequest request) {
    Usuario user = usuarios.findByUsernameIgnoreCase(request.username()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
    if (!user.isActivo() || user.getRol() != request.role() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
    user.registrarAcceso(); // Actualiza la fecha del último ingreso.
    return new LoginResponse(jwtService.generar(user), "Bearer", jwtService.expirationMinutes(), user.getId(), user.getUsername(), user.getRol());
  }

  // Retorna los datos y perfiles asociados a la sesión actualmente autenticada.
  public PerfilActualResponse perfilActual() {
    Usuario user = currentUser.required();
    return new PerfilActualResponse(user.getId(), user.getUsername(), user.getRol(), user.getPaciente() == null ? null : user.getPaciente().getId(), user.getMedico() == null ? null : user.getMedico().getId());
  }

  // Crea una cuenta de usuario con rol específico (exclusivo para el Administrador).
  @Transactional
  public Long crearUsuario(CrearUsuarioRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    if (usuarios.existsByUsernameIgnoreCase(request.username())) {
      throw new ApiException(HttpStatus.CONFLICT, "El usuario ya existe");
    }
    Paciente paciente = request.pacienteId() == null ? null : pacientes.findById(request.pacienteId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    Medico medico = request.medicoId() == null ? null : medicos.findById(request.medicoId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado"));
    return usuarios.save(new Usuario(request.username().trim(), passwordEncoder.encode(request.password()), request.rol(), paciente, medico)).getId();
  }

  // Registra un nuevo paciente en la clínica y le genera simultáneamente su usuario de acceso.
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
    Paciente guardado = pacienteService.registrarNuevoPaciente(paciente); // Sincroniza e inserta en el Árbol Binario de Pacientes.
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
