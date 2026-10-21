package com.utp.clinitech.controller;

import jakarta.validation.Valid; // Usado para activar las validaciones de los campos del DTO en la solicitud.
import java.net.URI; // Usado para retornar la URI del recurso recién creado en la cabecera Location.
import org.springframework.http.HttpStatus; // Usado para especificar códigos de estado HTTP en las respuestas.
import org.springframework.http.ResponseEntity; // Usado para construir respuestas HTTP completas con estado y cuerpo.
import org.springframework.web.bind.annotation.DeleteMapping; // Usado para mapear peticiones de eliminación definitiva.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear peticiones HTTP GET a métodos específicos.
import org.springframework.web.bind.annotation.PostMapping; // Usado para mapear peticiones HTTP POST a métodos específicos.
import org.springframework.web.bind.annotation.RequestBody; // Usado para vincular el cuerpo JSON de la petición con el objeto DTO.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para definir la ruta base del controlador de autenticación (/api/auth).
import org.springframework.web.bind.annotation.RestController; // Usado para declarar la clase como controlador REST que devuelve JSON.
import java.util.List; // Usado para devolver la lista de usuarios institucionales.
import org.springframework.web.bind.annotation.PatchMapping; // Usado para actualizaciones parciales como estado o clave.
import org.springframework.web.bind.annotation.PathVariable; // Usado para extraer el ID del usuario en la URL.
import org.springframework.web.bind.annotation.RequestParam; // Usado para filtrar usuarios por búsqueda y rol.
import org.springframework.web.bind.annotation.ResponseStatus; // Usado para asignar código 204 No Content.
import com.utp.clinitech.dto.auth.CambiarEstadoUsuarioRequest; // DTO para habilitar o deshabilitar usuarios.
import com.utp.clinitech.dto.auth.CrearUsuarioRequest; // Usado para recibir las credenciales al crear un usuario administrativo.
import com.utp.clinitech.dto.auth.LoginRequest; // Usado para recibir las credenciales y rol al iniciar sesión.
import com.utp.clinitech.dto.auth.LoginResponse; // Usado para devolver el token JWT y los datos del usuario autenticado.
import com.utp.clinitech.dto.auth.PerfilActualResponse; // Usado para devolver el perfil y permisos de la sesión actual.
import com.utp.clinitech.dto.auth.RegistroPacienteRequest; // Usado para recibir los datos de autoregistro de pacientes.
import com.utp.clinitech.dto.auth.ResetPasswordRequest; // DTO para restablecer contraseñas de usuarios.
import com.utp.clinitech.dto.auth.UsuarioResponse; // DTO para la respuesta con datos de usuarios.
import com.utp.clinitech.model.enums.RolUsuario; // Enum de roles de usuario.
import com.utp.clinitech.service.AuthService; // Usado para delegar las operaciones de autenticación y seguridad.

// Controlador REST para endpoints de autenticación, registro y gestión de usuarios.
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  // Constructor con inyección del servicio de autenticación.
  public AuthController(AuthService authService) { 
    this.authService = authService; 
  }

  // Endpoint para iniciar sesión y obtener el token JWT.
  @PostMapping("/login") 
  public LoginResponse login(@Valid @RequestBody LoginRequest request) { 
    return authService.login(request); 
  }

  // Endpoint público para el registro de nuevos pacientes con creación automática de cuenta.
  @PostMapping("/registro") 
  public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroPacienteRequest request) { 
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarPaciente(request)); 
  }

  // Endpoint para consultar la información del perfil y rol del usuario autenticado.
  @GetMapping("/me") 
  public PerfilActualResponse me() { 
    return authService.perfilActual(); 
  }

  // Endpoint restringido para que el administrador liste todos los usuarios institucionales.
  @GetMapping("/usuarios")
  public List<UsuarioResponse> listarUsuarios(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) RolUsuario rol) {
    return authService.listarUsuarios(q, rol);
  }

  // Endpoint restringido para que el administrador cree cuentas para recepcionistas, médicos o administradores.
  @PostMapping("/usuarios") 
  public ResponseEntity<Void> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) { 
    Long id = authService.crearUsuario(request); 
    return ResponseEntity.created(URI.create("/api/auth/usuarios/" + id)).build(); 
  }

  // Endpoint restringido para activar o desactivar una cuenta de usuario.
  @PatchMapping("/usuarios/{id}/estado")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoUsuarioRequest request) {
    authService.cambiarEstadoUsuario(id, request.activo());
  }

  // Endpoint restringido para restablecer la contraseña de una cuenta de usuario.
  @PatchMapping("/usuarios/{id}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetearPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetearPassword(id, request.password());
  }

  // Endpoint restringido para eliminar permanentemente una cuenta de usuario institucional.
  @DeleteMapping("/usuarios/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarUsuario(@PathVariable Long id) {
    authService.eliminarUsuario(id);
  }
}
