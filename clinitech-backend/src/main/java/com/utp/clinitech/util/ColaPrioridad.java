package com.utp.clinitech.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.enums.PrioridadCita;

/**
 * Montículo Binario (Min-Heap) propio para la gestión de citas en triaje
 * médico.
 * Prioriza citas de condición URGENTE frente a NORMAL, desempatando por fecha y
 * hora más temprana.
 */
public class ColaPrioridad {

  private final List<Cita> heap = new ArrayList<>();

  private final Comparator<Cita> comparador = (a, b) -> {
    int pa = a.getPrioridad() == PrioridadCita.URGENTE ? 0 : 1;
    int pb = b.getPrioridad() == PrioridadCita.URGENTE ? 0 : 1;
    if (pa != pb) {
      return Integer.compare(pa, pb);
    }
    if (a.getFechaHora() != null && b.getFechaHora() != null) {
      int fechaComp = a.getFechaHora().compareTo(b.getFechaHora());
      if (fechaComp != 0) {
        return fechaComp;
      }
    }
    Long idA = a.getId() != null ? a.getId() : 0L;
    Long idB = b.getId() != null ? b.getId() : 0L;
    return Long.compare(idA, idB);
  };

  public void encolar(Cita cita) {
    if (cita == null) {
      throw new IllegalArgumentException("La cita a encolar no puede ser nula");
    }
    heap.add(cita);
    flotar(heap.size() - 1);
  }

  public Optional<Cita> atenderSiguiente() {
    if (heap.isEmpty()) {
      return Optional.empty();
    }
    Cita primero = heap.get(0);
    Cita ultimo = heap.remove(heap.size() - 1);
    if (!heap.isEmpty()) {
      heap.set(0, ultimo);
      hundir(0);
    }
    return Optional.of(primero);
  }

  public Optional<Cita> siguiente() {
    return heap.isEmpty() ? Optional.empty() : Optional.of(heap.get(0));
  }

  public int tamanio() {
    return heap.size();
  }

  public boolean estaVacia() {
    return heap.isEmpty();
  }

  public List<Cita> ordenadas() {
    ColaPrioridad copia = new ColaPrioridad();
    for (Cita c : heap) {
      copia.encolar(c);
    }
    List<Cita> resultado = new ArrayList<>();
    while (!copia.estaVacia()) {
      resultado.add(copia.atenderSiguiente().orElseThrow());
    }
    return List.copyOf(resultado);
  }

  private void flotar(int indice) {
    while (indice > 0) {
      int padre = (indice - 1) / 2;
      if (comparador.compare(heap.get(indice), heap.get(padre)) < 0) {
        intercambiar(indice, padre);
        indice = padre;
      } else {
        break;
      }
    }
  }

  private void hundir(int indice) {
    int n = heap.size();
    while (indice < n) {
      int menor = indice;
      int izquierdo = 2 * indice + 1;
      int derecho = 2 * indice + 2;

      if (izquierdo < n && comparador.compare(heap.get(izquierdo), heap.get(menor)) < 0) {
        menor = izquierdo;
      }
      if (derecho < n && comparador.compare(heap.get(derecho), heap.get(menor)) < 0) {
        menor = derecho;
      }

      if (menor != indice) {
        intercambiar(indice, menor);
        indice = menor;
      } else {
        break;
      }
    }
  }

  private void intercambiar(int i, int j) {
    Cita temp = heap.get(i);
    heap.set(i, heap.get(j));
    heap.set(j, temp);
  }
}
