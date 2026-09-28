package com.utp.clinitech.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.utp.clinitech.dao.MedicoDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.dto.auth.LoginRequest;
import com.utp.clinitech.dto.auth.LoginResponse;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;
import com.utp.clinitech.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UsuarioDAO usuarioDAO;
  @Mock private PacienteDAO pacienteDAO;
  @Mock private MedicoDAO medicoDAO;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private CurrentUserService currentUser;
  @Mock private PacienteService pacienteService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(usuarioDAO, pacienteDAO, medicoDAO, passwordEncoder, jwtService, currentUser, pacienteService);
  }

  @Test
  @DisplayName("Login exitoso debe generar token JWT y devolver perfil")
  void testLoginExitoso() {
    Usuario usuario = new Usuario("admin", "hashCifrado", RolUsuario.ADMIN, null, null);

    when(usuarioDAO.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("CliniTech2026!", "hashCifrado")).thenReturn(true);
    when(jwtService.generar(usuario)).thenReturn("mocked.jwt.token");
    when(jwtService.expirationMinutes()).thenReturn(60L);

    LoginRequest request = LoginRequest.of("admin", "CliniTech2026!", "admin");
    LoginResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("mocked.jwt.token", response.accessToken());
    assertEquals("admin", response.username());
    assertEquals(RolUsuario.ADMIN, response.role());
  }

  @Test
  @DisplayName("Login con contraseña errónea debe lanzar ApiException UNAUTHORIZED")
  void testLoginPasswordErroneo() {
    Usuario usuario = new Usuario("admin", "hashCifrado", RolUsuario.ADMIN, null, null);

    when(usuarioDAO.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("clave_mala", "hashCifrado")).thenReturn(false);

    LoginRequest request = LoginRequest.of("admin", "clave_mala", "admin");

    ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
  }

  @Test
  @DisplayName("LoginRequest debe mapear correctamente roles en inglés y español")
  void testMapeoRolesFlexibles() {
    assertEquals(RolUsuario.PACIENTE, LoginRequest.of("p", "123", "patient").role());
    assertEquals(RolUsuario.PACIENTE, LoginRequest.of("p", "123", "paciente").role());
    assertEquals(RolUsuario.MEDICO, LoginRequest.of("m", "123", "doctor").role());
    assertEquals(RolUsuario.MEDICO, LoginRequest.of("m", "123", "medico").role());
    assertEquals(RolUsuario.ADMIN, LoginRequest.of("a", "123", "admin").role());
    assertEquals(RolUsuario.ADMIN, LoginRequest.of("a", "123", "ADMIN").role());
  }
}
