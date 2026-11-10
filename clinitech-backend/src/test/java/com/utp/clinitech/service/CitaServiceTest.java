package com.utp.clinitech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.utp.clinitech.dao.CitaDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dto.CambiarPrioridadCitaRequest;
import com.utp.clinitech.dto.CitaRequest;
import com.utp.clinitech.dto.CitaResponse;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.Especialidad;
import com.utp.clinitech.model.Medico;
import com.utp.clinitech.model.Paciente;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.PrioridadCita;
import com.utp.clinitech.model.enums.RolUsuario;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

  @Mock
  private CitaDAO citaDAO;

  @Mock
  private PacienteDAO pacienteDAO;

  @Mock
  private MedicoService medicoService;

  @Mock
  private CurrentUserService currentUser;

  private CitaService citaService;

  @BeforeEach
  void setUp() {
    citaService = new CitaService(citaDAO, pacienteDAO, medicoService, currentUser);
  }

  @Test
  @DisplayName("crear cita por PACIENTE debe forzar prioridad NORMAL aunque solicite URGENTE")
  void testCrearCitaPorPacienteFuerzaPrioridadNormal() {
    Paciente paciente = new Paciente();
    ReflectionTestUtils.setField(paciente, "id", 100L);

    Usuario pacienteUser = new Usuario("paciente1", "hash", RolUsuario.PACIENTE, paciente, null);
    when(currentUser.required()).thenReturn(pacienteUser);

    Medico medico = new Medico();
    ReflectionTestUtils.setField(medico, "id", 50L);
    ReflectionTestUtils.setField(medico, "nombres", "Carlos");
    ReflectionTestUtils.setField(medico, "apellidos", "Soto");
    Especialidad esp = new Especialidad("Cardiología", "Corazón");
    ReflectionTestUtils.setField(esp, "id", 1L);
    ReflectionTestUtils.setField(medico, "especialidad", esp);

    when(pacienteDAO.findById(100L)).thenReturn(Optional.of(paciente));
    when(medicoService.entidad(50L)).thenReturn(medico);
    when(citaDAO.existsByMedicoIdAndFechaHora(any(), any())).thenReturn(false);

    ArgumentCaptor<Cita> citaCaptor = ArgumentCaptor.forClass(Cita.class);
    when(citaDAO.save(citaCaptor.capture())).thenAnswer(invocation -> {
      Cita c = invocation.getArgument(0);
      ReflectionTestUtils.setField(c, "id", 999L);
      return c;
    });

    OffsetDateTime fechaHora = OffsetDateTime.now().plusDays(2);
    CitaRequest requestConUrgente = new CitaRequest(100L, 50L, fechaHora, PrioridadCita.URGENTE, "Dolor leve");

    CitaResponse response = citaService.crear(requestConUrgente);

    assertEquals(PrioridadCita.NORMAL, response.prioridad(), "La prioridad de una cita agendada por paciente debe forzarse a NORMAL");
    assertEquals(PrioridadCita.NORMAL, citaCaptor.getValue().getPrioridad());
  }

  @Test
  @DisplayName("cambiarPrioridad debe permitir a RECEPCIONISTA y ADMIN modificar la prioridad de una cita")
  void testCambiarPrioridadPermiteRecepcionista() {
    Paciente paciente = new Paciente();
    ReflectionTestUtils.setField(paciente, "id", 100L);
    Medico medico = new Medico();
    ReflectionTestUtils.setField(medico, "id", 50L);
    Especialidad esp = new Especialidad("Medicina General", "Atención primaria");
    ReflectionTestUtils.setField(medico, "especialidad", esp);

    Cita cita = new Cita(paciente, medico, OffsetDateTime.now().plusDays(1), PrioridadCita.NORMAL, "Fiebre alta");
    ReflectionTestUtils.setField(cita, "id", 333L);

    Usuario recepcionista = new Usuario("recep1", "hash", RolUsuario.RECEPCIONISTA, null, null);
    when(currentUser.required()).thenReturn(recepcionista);
    when(citaDAO.findById(333L)).thenReturn(Optional.of(cita));

    CitaResponse response = citaService.cambiarPrioridad(333L, new CambiarPrioridadCitaRequest(PrioridadCita.URGENTE));

    assertEquals(PrioridadCita.URGENTE, response.prioridad());
    assertEquals(PrioridadCita.URGENTE, cita.getPrioridad());
    verify(currentUser).requireRole(recepcionista, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);
  }

  @Test
  @DisplayName("cambiarPrioridad debe lanzar FORBIDDEN si el actor es PACIENTE o MEDICO")
  void testCambiarPrioridadRechazaRolesNoAutorizados() {
    Paciente paciente = new Paciente();
    ReflectionTestUtils.setField(paciente, "id", 100L);
    Medico medico = new Medico();
    ReflectionTestUtils.setField(medico, "id", 50L);

    Cita cita = new Cita(paciente, medico, OffsetDateTime.now().plusDays(1), PrioridadCita.NORMAL, "Control");
    ReflectionTestUtils.setField(cita, "id", 333L);

    Usuario medicoUser = new Usuario("medico1", "hash", RolUsuario.MEDICO, null, medico);
    when(currentUser.required()).thenReturn(medicoUser);
    when(citaDAO.findById(333L)).thenReturn(Optional.of(cita));
    doThrow(new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación"))
        .when(currentUser).requireRole(medicoUser, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);

    ApiException ex = assertThrows(ApiException.class,
        () -> citaService.cambiarPrioridad(333L, new CambiarPrioridadCitaRequest(PrioridadCita.URGENTE)));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
  }
}
