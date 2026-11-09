package com.utp.clinitech.util;

import java.util.ArrayList; // Usado para almacenar los resultados de los recorridos del árbol.
import java.util.List; // Usado para devolver listas de pacientes en los métodos de recorrido.
import java.util.Optional; // Usado para manejar la búsqueda de pacientes de manera segura, evitando nulls.
import java.util.concurrent.locks.ReentrantReadWriteLock; // Usado para garantizar sincronización y concurrencia segura entre múltiples hilos.
import com.utp.clinitech.model.Paciente; // Usado para representar a los pacientes en el árbol de búsqueda.

/**
 * Árbol Binario de Búsqueda (ABB) indexado por DNI para optimizar la búsqueda
 * de pacientes en memoria.
 * Implementa operaciones de inserción, búsqueda, eliminación y recorridos
 * (in-order, pre-order, post-order) con soporte para concurrencia segura (Thread-Safe).
 */
public class ArbolBusquedaPacientes {

  private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
  private Nodo raiz; // Nodo raíz del árbol, que representa el punto de entrada para todas las operaciones.

  public void insertar(Paciente paciente) {
    if (paciente == null || paciente.getDni() == null) { // Validación para asegurar que el paciente y su DNI no sean nulos antes de la inserción.
      throw new IllegalArgumentException("El paciente y su DNI no pueden ser nulos");
    }
    rwLock.writeLock().lock();
    try {
      raiz = insertar(raiz, paciente); // Llama al método recursivo privado para insertar el paciente en la posición correcta del árbol.
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  public Optional<Paciente> buscarPorDni(String dni) { // Método que permite buscar un paciente por su DNI.
    rwLock.readLock().lock();
    try {
      return buscarPorDniInterno(dni);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  private Optional<Paciente> buscarPorDniInterno(String dni) {
    if (dni == null) {
      return Optional.empty(); // Retorna un Optional vacío si el DNI proporcionado es nulo.
    }
    Nodo actual = raiz; // Comienza la búsqueda desde la raíz del árbol.
    while (actual != null) { // Itera mientras haya nodos por explorar en el árbol.
      int comparacion = dni.compareTo(actual.paciente.getDni()); // Compara el DNI buscado con el DNI del paciente en el nodo actual.
      if (comparacion == 0) { // Si los DNIs coinciden, se ha encontrado el paciente.
        return Optional.of(actual.paciente); // Retorna un Optional que contiene el paciente encontrado.
      }
      actual = comparacion < 0 ? actual.izquierdo : actual.derecho; // Navega al subárbol izquierdo o derecho.
    }
    return Optional.empty();
  }

  public boolean eliminar(String dni) { // Método que permite eliminar un paciente del árbol por su DNI de forma segura.
    if (dni == null) {
      return false;
    }
    rwLock.writeLock().lock();
    try {
      if (buscarPorDniInterno(dni).isEmpty()) {
        return false;
      }
      raiz = eliminar(raiz, dni);
      return true;
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  public List<Paciente> enOrden() { // Método que devuelve una lista de pacientes en orden ascendente según su DNI.
    rwLock.readLock().lock();
    try {
      List<Paciente> result = new ArrayList<>(); // Lista temporal para almacenar los pacientes durante el recorrido in-order.
      enOrden(raiz, result); // Llama al método recursivo privado para realizar el recorrido in-order.
      return List.copyOf(result); // Retorna una copia inmutable.
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public List<Paciente> preOrden() { // Método que devuelve una lista de pacientes en pre-orden.
    rwLock.readLock().lock();
    try {
      List<Paciente> result = new ArrayList<>();
      preOrden(raiz, result);
      return List.copyOf(result);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public List<Paciente> postOrden() { // Método que devuelve una lista de pacientes en post-orden.
    rwLock.readLock().lock();
    try {
      List<Paciente> result = new ArrayList<>();
      postOrden(raiz, result);
      return List.copyOf(result);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public int tamanio() { // Método que devuelve el número total de nodos.
    rwLock.readLock().lock();
    try {
      return contarNodos(raiz);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public int altura() { // Método que devuelve la altura del árbol.
    rwLock.readLock().lock();
    try {
      return calcularAltura(raiz);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public void limpiar() { // Método que elimina todos los nodos del árbol de forma thread-safe.
    rwLock.writeLock().lock();
    try {
      raiz = null;
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  private Nodo insertar(Nodo nodo, Paciente paciente) { // Método recursivo privado que inserta un paciente en el árbol, manteniendo la propiedad de búsqueda binaria.
    if (nodo == null) { // Si el nodo actual es nulo, significa que hemos encontrado la posición correcta para insertar el nuevo paciente.
      return new Nodo(paciente);
    }
    int comparacion = paciente.getDni().compareTo(nodo.paciente.getDni()); // Compara el DNI del paciente a insertar con el DNI del paciente en el nodo actual para decidir la dirección de inserción.
    if (comparacion < 0) {
      nodo.izquierdo = insertar(nodo.izquierdo, paciente);
    } else if (comparacion > 0) {
      nodo.derecho = insertar(nodo.derecho, paciente);
    } else {
      nodo.paciente = paciente; // Actualiza datos si el DNI ya existe
    }
    return nodo;
  }

  private Nodo eliminar(Nodo nodo, String dni) { // Método recursivo privado que elimina un paciente del árbol, manteniendo la propiedad de búsqueda binaria.
    if (nodo == null) {
      return null;
    }
    int comparacion = dni.compareTo(nodo.paciente.getDni()); // Compara el DNI del paciente a eliminar con el DNI del paciente en el nodo actual para decidir la dirección de eliminación.
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

  private Nodo obtenerMinimo(Nodo nodo) { // Método recursivo privado que encuentra el nodo con el valor mínimo en un subárbol.
    Nodo actual = nodo;
    while (actual.izquierdo != null) {
      actual = actual.izquierdo;
    }
    return actual;
  }

  private void enOrden(Nodo nodo, List<Paciente> result) { // Método recursivo privado que realiza un recorrido in-order del árbol, agregando los pacientes a la lista de resultados en orden ascendente según su DNI.
    if (nodo == null)
      return;
    enOrden(nodo.izquierdo, result);
    result.add(nodo.paciente);
    enOrden(nodo.derecho, result);
  }

  private void preOrden(Nodo nodo, List<Paciente> result) { // Método recursivo privado que realiza un recorrido pre-order del árbol, agregando los pacientes a la lista de resultados en el orden en que se visitan los nodos.
    if (nodo == null)
      return;
    result.add(nodo.paciente);
    preOrden(nodo.izquierdo, result);
    preOrden(nodo.derecho, result);
  }

  private void postOrden(Nodo nodo, List<Paciente> result) { // Método recursivo privado que realiza un recorrido post-order del árbol, agregando los pacientes a la lista de resultados en el orden en que se visitan los nodos.
    if (nodo == null)
      return;
    postOrden(nodo.izquierdo, result);
    postOrden(nodo.derecho, result);
    result.add(nodo.paciente);
  }

  private int contarNodos(Nodo nodo) { // Método recursivo privado que cuenta el número total de nodos (pacientes) en el árbol, sumando 1 por cada nodo visitado.
    if (nodo == null)
      return 0;
    return 1 + contarNodos(nodo.izquierdo) + contarNodos(nodo.derecho);
  }

  private int calcularAltura(Nodo nodo) { // Método recursivo privado que calcula la altura del árbol, definida como el número de niveles desde la raíz hasta la hoja más profunda. La altura de un árbol vacío es 0.
    if (nodo == null)
      return 0;
    return 1 + Math.max(calcularAltura(nodo.izquierdo), calcularAltura(nodo.derecho));
  }

  private static final class Nodo { // Clase interna privada que representa un nodo en el árbol binario de búsqueda. Cada nodo contiene un paciente y referencias a sus hijos izquierdo y derecho.
    private Paciente paciente;
    private Nodo izquierdo;
    private Nodo derecho;

    Nodo(Paciente paciente) {
      this.paciente = paciente;
    }
  }
}
