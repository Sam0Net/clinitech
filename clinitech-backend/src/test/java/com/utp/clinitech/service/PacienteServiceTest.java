package com.utp.clinitech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.utp.clinitech.dao.CitaDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dto.PacienteRequest;
import com.utp.clinitech.dto.PacienteResponse;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Paciente;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

  @Mock
  private PacienteDAO pacienteDAO;

  @Mock
  private CitaDAO citaDAO;

  @Mock
  private CurrentUserService currentUser;

  private PacienteService pacienteService;

  @BeforeEach
  void setUp() {
    pacienteService = new PacienteService(pacienteDAO, citaDAO, currentUser);
  }

  @Test
  @DisplayName("buscarPorDniEnArbol debe rechazar acceso a rol PACIENTE y requerir rol de personal (ADMIN, RECEPCIONISTA, MEDICO)")
  void testBuscarPorDniEnArbolRequiereRolesAdministrativos() {
    Usuario pacienteUser = new Usuario("paciente1", "hash", RolUsuario.PACIENTE, null, null);
    when(currentUser.required()).thenReturn(pacienteUser);
    doThrow(new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación"))
        .when(currentUser).requireRole(pacienteUser, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA, RolUsuario.MEDICO);

    ApiException ex = assertThrows(ApiException.class, () -> pacienteService.buscarPorDniEnArbol("12345678"));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
  }

  @Test
  @DisplayName("listarEnOrdenArbol debe rechazar acceso a rol PACIENTE y requerir rol de personal (ADMIN, RECEPCIONISTA, MEDICO)")
  void testListarEnOrdenArbolRequiereRolesAdministrativos() {
    Usuario pacienteUser = new Usuario("paciente1", "hash", RolUsuario.PACIENTE, null, null);
    when(currentUser.required()).thenReturn(pacienteUser);
    doThrow(new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación"))
        .when(currentUser).requireRole(pacienteUser, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA, RolUsuario.MEDICO);

    ApiException ex = assertThrows(ApiException.class, () -> pacienteService.listarEnOrdenArbol());
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
  }

  @Test
  @DisplayName("actualizar por PACIENTE debe preservar historial médico preexistente (inmutable)")
  void testActualizarPorPacientePreservaHistorialMedico() {
    Paciente pacienteExistente = new Paciente();
    ReflectionTestUtils.setField(pacienteExistente, "id", 10L);
    pacienteExistente.actualizar("87654321", "Juan", "Perez", LocalDate.of(1990, 1, 1),
        "987654321", "juan@test.com", "Av Principal 123", "Ninguna", "DIAGNOSTICO_MEDICO_OFICIAL");

    Usuario pacienteUser = new Usuario("juanp", "hash", RolUsuario.PACIENTE, pacienteExistente, null);

    when(currentUser.required()).thenReturn(pacienteUser);
    when(pacienteDAO.findById(10L)).thenReturn(Optional.of(pacienteExistente));

    PacienteRequest requestConIntentoDeModificarHistorial = new PacienteRequest(
        "87654321", "Juan Carlos", "Perez", LocalDate.of(1990, 1, 1),
        "987654321", "juan@test.com", "Av Nueva 456", "Penicilina", "TEXTO_FALSIFICADO_BORRADO"
    );

    PacienteResponse response = pacienteService.actualizar(10L, requestConIntentoDeModificarHistorial);

    assertEquals("DIAGNOSTICO_MEDICO_OFICIAL", response.historialMedico(), "El historial médico oficial debe mantenerse inalterable para el paciente");
    assertEquals("Av Nueva 456", response.direccion());
    assertEquals("Penicilina", response.alergias());
  }

  @Test
  @DisplayName("actualizar con DNI ya registrado por otro paciente debe lanzar CONFLICT")
  void testActualizarDniDuplicadoLanzaConflict() {
    Paciente pacienteExistente = new Paciente();
    ReflectionTestUtils.setField(pacienteExistente, "id", 10L);
    pacienteExistente.actualizar("11111111", "Ana", "Gomez", LocalDate.of(1995, 5, 5),
        "912345678", "ana@test.com", "Calle 1", null, null);

    Paciente otroPaciente = new Paciente();
    ReflectionTestUtils.setField(otroPaciente, "id", 20L);

    Usuario admin = new Usuario("admin", "hash", RolUsuario.ADMIN, null, null);

    when(currentUser.required()).thenReturn(admin);
    when(pacienteDAO.findById(10L)).thenReturn(Optional.of(pacienteExistente));
    when(pacienteDAO.findByDni("22222222")).thenReturn(Optional.of(otroPaciente));

    PacienteRequest request = new PacienteRequest(
        "22222222", "Ana", "Gomez", LocalDate.of(1995, 5, 5),
        "912345678", "ana@test.com", "Calle 1", null, null
    );

    ApiException ex = assertThrows(ApiException.class, () -> pacienteService.actualizar(10L, request));
    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
  }
}
