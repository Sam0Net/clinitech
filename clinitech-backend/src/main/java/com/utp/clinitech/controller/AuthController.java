package com.utp.clinitech.controller;

import jakarta.validation.Valid; // Usado para activar las validaciones de los campos del DTO en la solicitud.
import java.net.URI; // Usado para retornar la URI del recurso recién creado en la cabecera Location.
import org.springframework.http.HttpStatus; // Usado para especificar códigos de estado HTTP en las respuestas.
import org.springframework.http.ResponseEntity; // Usado para construir respuestas HTTP completas con estado y cuerpo.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear peticiones HTTP GET a métodos específicos.
import org.springframework.web.bind.annotation.PostMapping; // Usado para mapear peticiones HTTP POST a métodos específicos.
import org.springframework.web.bind.annotation.RequestBody; // Usado para vincular el cuerpo JSON de la petición con el objeto DTO.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para definir la ruta base del controlador de autenticación (/api/auth).
import org.springframework.web.bind.annotation.RestController; // Usado para declarar la clase como controlador REST que devuelve JSON.
import com.utp.clinitech.dto.auth.CrearUsuarioRequest; // Usado para recibir las credenciales al crear un usuario administrativo.
import com.utp.clinitech.dto.auth.LoginRequest; // Usado para recibir las credenciales y rol al iniciar sesión.
import com.utp.clinitech.dto.auth.LoginResponse; // Usado para devolver el token JWT y los datos del usuario autenticado.
import com.utp.clinitech.dto.auth.PerfilActualResponse; // Usado para devolver el perfil y permisos de la sesión actual.
import com.utp.clinitech.dto.auth.RegistroPacienteRequest; // Usado para recibir los datos de autoregistro de pacientes.
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

  // Endpoint restringido para que el administrador cree cuentas para médicos o personal.
  @PostMapping("/usuarios") 
  public ResponseEntity<Void> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) { 
    Long id = authService.crearUsuario(request); 
    return ResponseEntity.created(URI.create("/api/auth/usuarios/" + id)).build(); 
  }
}
