package com.utp.clinitech.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.auth.*;
import com.utp.clinitech.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) { this.authService = authService; }
  @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
  @PostMapping("/registro") public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroPacienteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarPaciente(request)); }
  @GetMapping("/me") public PerfilActualResponse me() { return authService.perfilActual(); }
  @PostMapping("/usuarios") public ResponseEntity<Void> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) { Long id = authService.crearUsuario(request); return ResponseEntity.created(java.net.URI.create("/api/auth/usuarios/" + id)).build(); }
}
