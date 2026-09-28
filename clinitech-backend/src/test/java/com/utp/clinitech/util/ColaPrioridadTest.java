package com.utp.clinitech.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.enums.PrioridadCita;

class ColaPrioridadTest {

  private ColaPrioridad cola;

  @BeforeEach
  void setUp() {
    cola = new ColaPrioridad();
  }

  private Cita crearCita(PrioridadCita prioridad, OffsetDateTime fechaHora, String motivo) {
    return new Cita(null, null, fechaHora, prioridad, motivo);
  }

  @Test
  @DisplayName("Las citas URGENTES deben ser atendidas antes que las NORMALES sin importar el orden de llegada")
  void testPrioridadUrgentePrimero() {
    OffsetDateTime hora1 = OffsetDateTime.of(2026, 9, 20, 9, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime hora2 = OffsetDateTime.of(2026, 9, 20, 9, 30, 0, 0, ZoneOffset.UTC);
    OffsetDateTime hora3 = OffsetDateTime.of(2026, 9, 20, 10, 0, 0, 0, ZoneOffset.UTC);

    Cita c1Normal = crearCita(PrioridadCita.NORMAL, hora1, "Chequeo rutinario");
    Cita c2Normal = crearCita(PrioridadCita.NORMAL, hora2, "Revisión");
    Cita c3Urgente = crearCita(PrioridadCita.URGENTE, hora3, "Dolor torácico agudo");

    cola.encolar(c1Normal);
    cola.encolar(c2Normal);
    cola.encolar(c3Urgente);

    assertEquals(3, cola.tamanio());
    assertFalse(cola.estaVacia());

    // La primera en salir debe ser la urgente
    Optional<Cita> atendida1 = cola.atenderSiguiente();
    assertTrue(atendida1.isPresent());
    assertEquals(PrioridadCita.URGENTE, atendida1.get().getPrioridad());
    assertEquals("Dolor torácico agudo", atendida1.get().getMotivoConsulta());

    // Luego deben salir las normales ordenadas por fechaHora
    Optional<Cita> atendida2 = cola.atenderSiguiente();
    assertTrue(atendida2.isPresent());
    assertEquals("Chequeo rutinario", atendida2.get().getMotivoConsulta());

    Optional<Cita> atendida3 = cola.atenderSiguiente();
    assertTrue(atendida3.isPresent());
    assertEquals("Revisión", atendida3.get().getMotivoConsulta());

    assertTrue(cola.estaVacia());
    assertFalse(cola.atenderSiguiente().isPresent());
  }

  @Test
  @DisplayName("Entre citas de igual prioridad debe atenderse la que tenga horario más temprano")
  void testDesempatePorHorario() {
    OffsetDateTime temprano = OffsetDateTime.of(2026, 9, 20, 8, 30, 0, 0, ZoneOffset.UTC);
    OffsetDateTime tarde = OffsetDateTime.of(2026, 9, 20, 11, 0, 0, 0, ZoneOffset.UTC);

    Cita urgenteTarde = crearCita(PrioridadCita.URGENTE, tarde, "Urgente tarde");
    Cita urgenteTemprano = crearCita(PrioridadCita.URGENTE, temprano, "Urgente temprano");

    cola.encolar(urgenteTarde);
    cola.encolar(urgenteTemprano);

    assertEquals("Urgente temprano", cola.atenderSiguiente().orElseThrow().getMotivoConsulta());
    assertEquals("Urgente tarde", cola.atenderSiguiente().orElseThrow().getMotivoConsulta());
  }

  @Test
  @DisplayName("El método ordenadas debe devolver la lista ordenada sin vaciar la cola")
  void testOrdenadasSinVaciarCola() {
    OffsetDateTime h1 = OffsetDateTime.of(2026, 9, 20, 9, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime h2 = OffsetDateTime.of(2026, 9, 20, 10, 0, 0, 0, ZoneOffset.UTC);

    cola.encolar(crearCita(PrioridadCita.NORMAL, h1, "N1"));
    cola.encolar(crearCita(PrioridadCita.URGENTE, h2, "U1"));

    List<Cita> lista = cola.ordenadas();
    assertEquals(2, lista.size());
    assertEquals("U1", lista.get(0).getMotivoConsulta());
    assertEquals("N1", lista.get(1).getMotivoConsulta());

    // La cola original no se modifica
    assertEquals(2, cola.tamanio());
  }
}
