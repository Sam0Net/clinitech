package com.utp.clinitech.util;

import java.util.ArrayList; // Usado para representar el arreglo dinámico que almacena los elementos del montículo (Heap).
import java.util.Comparator; // Usado para definir el criterio de comparación y prioridad entre citas médicas.
import java.util.List; // Usado para devolver y manipular colecciones ordenadas de citas.
import java.util.Optional; // Usado para retornar citas de manera segura evitando el retorno de valores nulos.
import com.utp.clinitech.model.Cita; // Usado para modelar los elementos de citas médicas gestionados en la cola.
import com.utp.clinitech.model.enums.PrioridadCita; // Usado para evaluar la severidad (URGENTE o NORMAL) en el triaje.

/**
 * Montículo Binario (Min-Heap) propio para la gestión de citas en triaje
 * médico.
 * Prioriza citas de condición URGENTE frente a NORMAL, desempatando por fecha y
 * hora más temprana.
 */
public class ColaPrioridad {

  // Estructura interna que almacena el árbol binario completo en un arreglo dinámico.
  private final List<Cita> heap = new ArrayList<>();

  // Comparador personalizado que define la regla de prioridad en tiempo O(1).
  private final Comparator<Cita> comparador = (a, b) -> {
    int pa = a.getPrioridad() == PrioridadCita.URGENTE ? 0 : 1;
    int pb = b.getPrioridad() == PrioridadCita.URGENTE ? 0 : 1;
    if (pa != pb) {
      return Integer.compare(pa, pb); // Prioridad médica tiene máxima precedencia.
    }
    if (a.getFechaHora() != null && b.getFechaHora() != null) {
      int fechaComp = a.getFechaHora().compareTo(b.getFechaHora());
      if (fechaComp != 0) {
        return fechaComp; // Desempate por hora más temprana.
      }
    }
    Long idA = a.getId() != null ? a.getId() : 0L;
    Long idB = b.getId() != null ? b.getId() : 0L;
    return Long.compare(idA, idB); // Desempate determinista por ID.
  };

  // Inserta una nueva cita en el montículo en tiempo O(log n).
  public void encolar(Cita cita) {
    if (cita == null) { // Valida que no se inserten elementos nulos en el montículo.
      throw new IllegalArgumentException("La cita a encolar no puede ser nula");
    }
    heap.add(cita); // Agrega el elemento al final del arreglo.
    flotar(heap.size() - 1); // Reordena hacia arriba (sift-up) para restaurar la propiedad de Heap.
  }

  // Extrae y atiende la cita de mayor prioridad (la raíz) en tiempo O(log n).
  public Optional<Cita> atenderSiguiente() {
    if (heap.isEmpty()) { // Verifica si no hay citas pendientes por atender.
      return Optional.empty();
    }
    Cita primero = heap.get(0); // Guarda la cita con mayor urgencia (en la raíz).
    Cita ultimo = heap.remove(heap.size() - 1); // Extrae el último elemento del arreglo.
    if (!heap.isEmpty()) {
      heap.set(0, ultimo); // Mueve el último elemento a la raíz.
      hundir(0); // Reordena hacia abajo (sift-down) para restaurar la propiedad de Heap.
    }
    return Optional.of(primero); // Retorna la cita atendida envuelta en Optional.
  }

  // Consulta la cita en la cima sin extraerla de la cola en tiempo O(1).
  public Optional<Cita> siguiente() {
    return heap.isEmpty() ? Optional.empty() : Optional.of(heap.get(0));
  }

  // Retorna la cantidad de citas actualmente en la cola de prioridad.
  public int tamanio() {
    return heap.size();
  }

  // Comprueba si la cola no contiene ninguna cita médica.
  public boolean estaVacia() {
    return heap.isEmpty();
  }

  // Retorna una lista inmutable con todas las citas ordenadas por prioridad usando Heap-Sort.
  public List<Cita> ordenadas() {
    ColaPrioridad copia = new ColaPrioridad(); // Crea una copia para no vaciar la cola original.
    for (Cita c : heap) {
      copia.encolar(c);
    }
    List<Cita> resultado = new ArrayList<>();
    while (!copia.estaVacia()) { // Extrae sucesivamente el elemento prioritario.
      resultado.add(copia.atenderSiguiente().orElseThrow());
    }
    return List.copyOf(resultado); // Retorna una copia de lista inmutable.
  }

  // Algoritmo de flotación (sift-up): asciende un nodo comparándolo con su padre hasta su posición correcta.
  private void flotar(int indice) {
    while (indice > 0) {
      int padre = (indice - 1) / 2; // Calcula el índice del nodo padre en el montículo binario.
      if (comparador.compare(heap.get(indice), heap.get(padre)) < 0) { // Si el hijo tiene mayor prioridad que el padre:
        intercambiar(indice, padre); // Intercambia las posiciones en el arreglo.
        indice = padre; // Continúa la evaluación desde la posición del padre.
      } else {
        break; // La propiedad del Min-Heap ya se cumple.
      }
    }
  }

  // Algoritmo de hundimiento (sift-down): desciende un nodo comparándolo con sus hijos izquierdo y derecho.
  private void hundir(int indice) {
    int n = heap.size();
    while (indice < n) {
      int menor = indice; // Asume inicialmente que el nodo actual es el de mayor prioridad.
      int izquierdo = 2 * indice + 1; // Índice del hijo izquierdo en el arreglo.
      int derecho = 2 * indice + 2; // Índice del hijo derecho en el arreglo.

      // Comprueba si el hijo izquierdo existe y tiene mayor prioridad.
      if (izquierdo < n && comparador.compare(heap.get(izquierdo), heap.get(menor)) < 0) {
        menor = izquierdo;
      }
      // Comprueba si el hijo derecho existe y tiene mayor prioridad que el menor actual.
      if (derecho < n && comparador.compare(heap.get(derecho), heap.get(menor)) < 0) {
        menor = derecho;
      }

      // Si alguno de los hijos tiene mayor prioridad, intercambia y continúa descendiendo.
      if (menor != indice) {
        intercambiar(indice, menor);
        indice = menor;
      } else {
        break; // El elemento está en su posición válida en el montículo.
      }
    }
  }

  // Intercambia dos elementos en los índices i y j del arreglo del montículo en O(1).
  private void intercambiar(int i, int j) {
    Cita temp = heap.get(i);
    heap.set(i, heap.get(j));
    heap.set(j, temp);
  }
}
