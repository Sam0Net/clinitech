package com.utp.clinitech.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.utp.clinitech.model.Paciente;

class ArbolBusquedaPacientesTest {

  private ArbolBusquedaPacientes arbol;

  @BeforeEach
  void setUp() {
    arbol = new ArbolBusquedaPacientes();
  }

  private Paciente crearPaciente(String dni, String nombres, String apellidos) {
    Paciente p = new Paciente();
    p.actualizar(dni, nombres, apellidos, LocalDate.of(1990, 1, 1), "999888777", dni + "@test.com", "Av. Prueba 123", "Ninguna", "Sano");
    return p;
  }

  @Test
  @DisplayName("Debe insertar pacientes y encontrarlos por DNI en O(log n)")
  void testInsertarYBuscar() {
    Paciente p1 = crearPaciente("40000000", "Carlos", "Perez");
    Paciente p2 = crearPaciente("20000000", "Ana", "Gomez");
    Paciente p3 = crearPaciente("60000000", "Luis", "Ruiz");

    arbol.insertar(p1);
    arbol.insertar(p2);
    arbol.insertar(p3);

    assertEquals(3, arbol.tamanio());

    Optional<Paciente> encontrado = arbol.buscarPorDni("20000000");
    assertTrue(encontrado.isPresent());
    assertEquals("Ana", encontrado.get().getNombres());

    Optional<Paciente> noExiste = arbol.buscarPorDni("99999999");
    assertFalse(noExiste.isPresent());
  }

  @Test
  @DisplayName("El recorrido in-order debe devolver los pacientes ordenados ascendentemente por DNI")
  void testRecorridoInOrderOrdenado() {
    arbol.insertar(crearPaciente("50000000", "E", "Cinco"));
    arbol.insertar(crearPaciente("20000000", "B", "Dos"));
    arbol.insertar(crearPaciente("80000000", "H", "Ocho"));
    arbol.insertar(crearPaciente("10000000", "A", "Uno"));
    arbol.insertar(crearPaciente("30000000", "C", "Tres"));

    List<Paciente> ordenados = arbol.enOrden();
    assertEquals(5, ordenados.size());
    assertEquals("10000000", ordenados.get(0).getDni());
    assertEquals("20000000", ordenados.get(1).getDni());
    assertEquals("30000000", ordenados.get(2).getDni());
    assertEquals("50000000", ordenados.get(3).getDni());
    assertEquals("80000000", ordenados.get(4).getDni());
  }

  @Test
  @DisplayName("Debe eliminar nodos hoja, con un hijo y con dos hijos correctamente")
  void testEliminarNodo() {
    arbol.insertar(crearPaciente("50000000", "E", "Cinco"));
    arbol.insertar(crearPaciente("30000000", "C", "Tres"));
    arbol.insertar(crearPaciente("70000000", "G", "Siete"));
    arbol.insertar(crearPaciente("20000000", "B", "Dos"));
    arbol.insertar(crearPaciente("40000000", "D", "Cuatro"));

    // Eliminar hoja
    assertTrue(arbol.eliminar("20000000"));
    assertFalse(arbol.buscarPorDni("20000000").isPresent());
    assertEquals(4, arbol.tamanio());

    // Eliminar con dos hijos (nodo raíz)
    assertTrue(arbol.eliminar("50000000"));
    assertFalse(arbol.buscarPorDni("50000000").isPresent());
    assertEquals(3, arbol.tamanio());

    // Verificar que el orden in-order se mantiene
    List<Paciente> restantes = arbol.enOrden();
    assertEquals("30000000", restantes.get(0).getDni());
    assertEquals("40000000", restantes.get(1).getDni());
    assertEquals("70000000", restantes.get(2).getDni());
  }

  @Test
  @DisplayName("Debe calcular correctamente la altura del árbol")
  void testCalcularAltura() {
    assertEquals(0, arbol.altura());

    arbol.insertar(crearPaciente("50000000", "E", "Cinco"));
    assertEquals(1, arbol.altura());

    arbol.insertar(crearPaciente("30000000", "C", "Tres"));
    assertEquals(2, arbol.altura());

    arbol.insertar(crearPaciente("20000000", "B", "Dos"));
    assertEquals(3, arbol.altura());
  }
}
