package com.utp.clinitech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

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
  @DisplayName("Login exitoso sin especificar rol debe reconocer automáticamente el perfil")
  void testLoginExitosoSinRolEspecificado() {
    Usuario usuario = new Usuario("doctor1", "hashCifrado", RolUsuario.MEDICO, null, null);

    when(usuarioDAO.findByUsernameIgnoreCase("doctor1")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("123456", "hashCifrado")).thenReturn(true);
    when(jwtService.generar(usuario)).thenReturn("mocked.jwt.token.doc");
    when(jwtService.expirationMinutes()).thenReturn(60L);

    LoginRequest request = new LoginRequest("doctor1", "123456", null);
    LoginResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("mocked.jwt.token.doc", response.accessToken());
    assertEquals("doctor1", response.username());
    assertEquals(RolUsuario.MEDICO, response.role());
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
    assertEquals(RolUsuario.RECEPCIONISTA, LoginRequest.of("r", "123", "recepcionista").role());
    assertEquals(RolUsuario.RECEPCIONISTA, LoginRequest.of("r", "123", "recepcion").role());
    assertEquals(RolUsuario.RECEPCIONISTA, LoginRequest.of("r", "123", "receptionist").role());
  }

  @Test
  @DisplayName("Eliminar cuenta propia de administrador debe fallar con BAD_REQUEST")
  void testEliminarUsuarioPropiaCuentaDebeFallar() {
    Usuario admin = new Usuario("admin", "hash", RolUsuario.ADMIN, null, null);
    ReflectionTestUtils.setField(admin, "id", 1L);

    when(currentUser.required()).thenReturn(admin);

    ApiException ex = assertThrows(ApiException.class, () -> authService.eliminarUsuario(1L));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
  }

  @Test
  @DisplayName("Eliminar cuenta de otro usuario debe ejecutarse correctamente")
  void testEliminarUsuarioExitoso() {
    Usuario admin = new Usuario("admin", "hash", RolUsuario.ADMIN, null, null);
    ReflectionTestUtils.setField(admin, "id", 1L);

    Usuario otro = new Usuario("recepcion1", "hash", RolUsuario.RECEPCIONISTA, null, null);
    ReflectionTestUtils.setField(otro, "id", 2L);

    when(currentUser.required()).thenReturn(admin);
    when(usuarioDAO.findById(2L)).thenReturn(Optional.of(otro));

    authService.eliminarUsuario(2L);

    verify(usuarioDAO).delete(otro);
  }
}
