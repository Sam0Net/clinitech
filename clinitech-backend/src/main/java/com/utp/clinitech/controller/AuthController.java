package com.utp.clinitech.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.utp.clinitech.dto.auth.CrearUsuarioRequest;
import com.utp.clinitech.dto.auth.LoginRequest;
import com.utp.clinitech.dto.auth.LoginResponse;
import com.utp.clinitech.dto.auth.PerfilActualResponse;
import com.utp.clinitech.dto.auth.RegistroPacienteRequest;
import com.utp.clinitech.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) { this.authService = authService; }
  @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
  @PostMapping("/registro") public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroPacienteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarPaciente(request)); }
  @GetMapping("/me") public PerfilActualResponse me() { return authService.perfilActual(); }
  @PostMapping("/usuarios") public ResponseEntity<Void> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) { Long id = authService.crearUsuario(request); return ResponseEntity.created(URI.create("/api/auth/usuarios/" + id)).build(); }
}
