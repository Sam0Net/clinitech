package com.utp.clinitech.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.utp.clinitech.model.Paciente;

/**
 * Árbol Binario de Búsqueda (ABB) indexado por DNI para optimizar la búsqueda de pacientes en memoria.
 * Implementa operaciones de inserción, búsqueda, eliminación y recorridos (in-order, pre-order, post-order).
 */
public class ArbolBusquedaPacientes {

  private Nodo raiz;

  public void insertar(Paciente paciente) {
    if (paciente == null || paciente.getDni() == null) {
      throw new IllegalArgumentException("El paciente y su DNI no pueden ser nulos");
    }
    raiz = insertar(raiz, paciente);
  }

  public Optional<Paciente> buscarPorDni(String dni) {
    if (dni == null) {
      return Optional.empty();
    }
    Nodo actual = raiz;
    while (actual != null) {
      int comparacion = dni.compareTo(actual.paciente.getDni());
      if (comparacion == 0) {
        return Optional.of(actual.paciente);
      }
      actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
    }
    return Optional.empty();
  }

  public boolean eliminar(String dni) {
    if (dni == null || !buscarPorDni(dni).isPresent()) {
      return false;
    }
    raiz = eliminar(raiz, dni);
    return true;
  }

  public List<Paciente> enOrden() {
    List<Paciente> result = new ArrayList<>();
    enOrden(raiz, result);
    return List.copyOf(result);
  }

  public List<Paciente> preOrden() {
    List<Paciente> result = new ArrayList<>();
    preOrden(raiz, result);
    return List.copyOf(result);
  }

  public List<Paciente> postOrden() {
    List<Paciente> result = new ArrayList<>();
    postOrden(raiz, result);
    return List.copyOf(result);
  }

  public int tamanio() {
    return contarNodos(raiz);
  }

  public int altura() {
    return calcularAltura(raiz);
  }

  public void limpiar() {
    raiz = null;
  }

  private Nodo insertar(Nodo nodo, Paciente paciente) {
    if (nodo == null) {
      return new Nodo(paciente);
    }
    int comparacion = paciente.getDni().compareTo(nodo.paciente.getDni());
    if (comparacion < 0) {
      nodo.izquierdo = insertar(nodo.izquierdo, paciente);
    } else if (comparacion > 0) {
      nodo.derecho = insertar(nodo.derecho, paciente);
    } else {
      nodo.paciente = paciente; // Actualiza datos si el DNI ya existe
    }
    return nodo;
  }

  private Nodo eliminar(Nodo nodo, String dni) {
    if (nodo == null) {
      return null;
    }
    int comparacion = dni.compareTo(nodo.paciente.getDni());
    if (comparacion < 0) {
      nodo.izquierdo = eliminar(nodo.izquierdo, dni);
    } else if (comparacion > 0) {
      nodo.derecho = eliminar(nodo.derecho, dni);
    } else {
      // Caso 1 y 2: 0 o 1 hijo
      if (nodo.izquierdo == null) {
        return nodo.derecho;
      } else if (nodo.derecho == null) {
        return nodo.izquierdo;
      }
      // Caso 3: 2 hijos - sucesor in-order (mínimo del subárbol derecho)
      Nodo sucesor = obtenerMinimo(nodo.derecho);
      nodo.paciente = sucesor.paciente;
      nodo.derecho = eliminar(nodo.derecho, sucesor.paciente.getDni());
    }
    return nodo;
  }

  private Nodo obtenerMinimo(Nodo nodo) {
    Nodo actual = nodo;
    while (actual.izquierdo != null) {
      actual = actual.izquierdo;
    }
    return actual;
  }

  private void enOrden(Nodo nodo, List<Paciente> result) {
    if (nodo == null) return;
    enOrden(nodo.izquierdo, result);
    result.add(nodo.paciente);
    enOrden(nodo.derecho, result);
  }

  private void preOrden(Nodo nodo, List<Paciente> result) {
    if (nodo == null) return;
    result.add(nodo.paciente);
    preOrden(nodo.izquierdo, result);
    preOrden(nodo.derecho, result);
  }

  private void postOrden(Nodo nodo, List<Paciente> result) {
    if (nodo == null) return;
    postOrden(nodo.izquierdo, result);
    postOrden(nodo.derecho, result);
    result.add(nodo.paciente);
  }

  private int contarNodos(Nodo nodo) {
    if (nodo == null) return 0;
    return 1 + contarNodos(nodo.izquierdo) + contarNodos(nodo.derecho);
  }

  private int calcularAltura(Nodo nodo) {
    if (nodo == null) return 0;
    return 1 + Math.max(calcularAltura(nodo.izquierdo), calcularAltura(nodo.derecho));
  }

  private static final class Nodo {
    private Paciente paciente;
    private Nodo izquierdo;
    private Nodo derecho;

    Nodo(Paciente paciente) {
      this.paciente = paciente;
    }
  }
}
