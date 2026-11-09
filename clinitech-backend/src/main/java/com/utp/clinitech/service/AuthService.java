package com.utp.clinitech.service;

import java.util.List; // Usado para devolver colecciones de usuarios en la administración.
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
import com.utp.clinitech.dto.auth.UsuarioResponse; // Usado para devolver los datos de usuarios en la gestión administrativa.
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

  // Hash BCrypt sintético precalculado para igualar el tiempo de cómputo en intentos con usuarios inexistentes (Anti-Timing Attack).
  private static final String DUMMY_BCRYPT_HASH = "$2a$10$w8.dG5y7pYfB19pGskKjAe0vH6Yj3Z9tU0H3U4e5f6g7h8i9j0k1l";

  // Autentica credenciales de usuario, verifica rol solicitado y emite un token JWT.
  @Transactional
  public LoginResponse login(LoginRequest request) {
    java.util.Optional<Usuario> userOpt = usuarios.findByUsernameIgnoreCase(request.username());
    if (userOpt.isEmpty()) {
      // Simula el cálculo de BCrypt para prevenir la enumeración de nombres de usuario mediante análisis de latencia
      passwordEncoder.matches(request.password(), DUMMY_BCRYPT_HASH);
      throw new ApiException(HttpStatus.UNAUTHORIZED, "El usuario o la contraseña ingresados son incorrectos. Por favor, verifica tus datos e inténtalo nuevamente.");
    }

    Usuario user = userOpt.get();
    boolean passwordValida = passwordEncoder.matches(request.password(), user.getPasswordHash());
    if (!user.isActivo() || (request.role() != null && user.getRol() != request.role()) || !passwordValida) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "El usuario o la contraseña ingresados son incorrectos. Por favor, verifica tus datos e inténtalo nuevamente.");
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

  // Lista los usuarios del sistema para la gestión administrativa (exclusivo para el Administrador).
  public List<UsuarioResponse> listarUsuarios(String q, RolUsuario rol) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    List<Usuario> list = rol != null ? usuarios.findByRolOrderByIdDesc(rol) : usuarios.findAllByOrderByIdDesc();
    if (q != null && !q.isBlank()) {
      String query = q.trim().toLowerCase();
      list = list.stream().filter(u -> {
        boolean matchUsername = u.getUsername() != null && u.getUsername().toLowerCase().contains(query);
        boolean matchMedico = u.getMedico() != null && (u.getMedico().getNombres() + " " + u.getMedico().getApellidos()).toLowerCase().contains(query);
        boolean matchPaciente = u.getPaciente() != null && (u.getPaciente().getNombres() + " " + u.getPaciente().getApellidos()).toLowerCase().contains(query);
        return matchUsername || matchMedico || matchPaciente;
      }).toList();
    }
    return list.stream().map(UsuarioResponse::from).toList();
  }

  // Activa o desactiva una cuenta de usuario institucional.
  @Transactional
  public void cambiarEstadoUsuario(Long id, boolean activo) {
    Usuario current = currentUser.required();
    currentUser.requireRole(current, RolUsuario.ADMIN);
    if (current.getId().equals(id) && !activo) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta de administrador activa.");
    }
    Usuario user = usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.cambiarEstado(activo);
  }

  // Restablece la contraseña de acceso de una cuenta institucional.
  @Transactional
  public void resetearPassword(Long id, String nuevaPassword) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Usuario user = usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.cambiarPassword(passwordEncoder.encode(nuevaPassword));
  }

  // Elimina una cuenta de usuario del sistema y limpia el registro de paciente asociado si no posee citas (exclusivo para el Administrador).
  @Transactional
  public void eliminarUsuario(Long id) {
    Usuario current = currentUser.required();
    currentUser.requireRole(current, RolUsuario.ADMIN);
    if (current.getId().equals(id)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta de administrador activa.");
    }
    Usuario user = usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    Paciente paciente = user.getPaciente();
    usuarios.delete(user);
    if (paciente != null && user.getRol() == RolUsuario.PACIENTE) {
      pacienteService.eliminarSiNoTieneCitas(paciente.getId());
    }
  }
}
