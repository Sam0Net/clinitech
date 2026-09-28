package com.utp.clinitech.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrafoEspecialidadesTest {

  private GrafoEspecialidades grafo;

  @BeforeEach
  void setUp() {
    grafo = new GrafoEspecialidades();
  }

  @Test
  @DisplayName("Debe registrar y relacionar especialidades bidireccionalmente")
  void testRelacionarEspecialidades() {
    grafo.relacionar(1L, 2L); // Medicina General <-> Cardiología
    grafo.relacionar(1L, 3L); // Medicina General <-> Pediatría

    assertTrue(grafo.existeConexion(1L, 2L));
    assertTrue(grafo.existeConexion(2L, 1L));
    assertTrue(grafo.existeConexion(1L, 3L));
    assertFalse(grafo.existeConexion(2L, 3L));

    Set<Long> relacionadasMedGen = grafo.relacionadasCon(1L);
    assertEquals(2, relacionadasMedGen.size());
    assertTrue(relacionadasMedGen.contains(2L));
    assertTrue(relacionadasMedGen.contains(3L));
  }

  @Test
  @DisplayName("Debe encontrar la ruta más corta (BFS) para derivación entre especialidades")
  void testRutaMasCortaBFS() {
    // Red de derivaciones:
    // 1 (Med. General) - 2 (Cardiología) - 4 (Cirugía Cardiovascular)
    // 1 (Med. General) - 3 (Pediatría)
    grafo.relacionar(1L, 2L);
    grafo.relacionar(2L, 4L);
    grafo.relacionar(1L, 3L);

    // Ruta de 3 (Pediatría) a 4 (Cirugía Cardiovascular): 3 -> 1 -> 2 -> 4
    List<Long> ruta = grafo.rutaMasCorta(3L, 4L);
    assertEquals(List.of(3L, 1L, 2L, 4L), ruta);

    // Ruta directa de 1 a 2: 1 -> 2
    List<Long> rutaDirecta = grafo.rutaMasCorta(1L, 2L);
    assertEquals(List.of(1L, 2L), rutaDirecta);
  }

  @Test
  @DisplayName("Debe devolver lista vacía cuando no exista camino entre dos especialidades")
  void testRutaInexistente() {
    grafo.agregarEspecialidad(1L);
    grafo.agregarEspecialidad(5L); // Nodo desconectado

    List<Long> ruta = grafo.rutaMasCorta(1L, 5L);
    assertTrue(ruta.isEmpty());
  }

  @Test
  @DisplayName("No debe permitir auto-relación en la misma especialidad")
  void testAutoRelacion() {
    assertThrows(IllegalArgumentException.class, () -> grafo.relacionar(1L, 1L));
  }
}
