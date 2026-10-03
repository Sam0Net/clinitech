package com.utp.clinitech.util;

import java.util.HashMap; // Implementar lista de adyacencia.
import java.util.Map; // Implementar lista de adyacencia.
import java.util.Set; // Evitar especialidades duplicadas en la lista de adyacencia.
import java.util.HashSet; // Evitar especialidades duplicadas en la lista de adyacencia.
import java.util.LinkedList; // Para reconstruir la ruta más corta desde el destino hasta el origen.
import java.util.List;
import java.util.Queue; // Cola FIFO para exploración de niveles en BFS.
import java.util.ArrayDeque; // Cola FIFO para exploración de niveles en BFS.

public class GrafoEspecialidades {
  private final Map<Long, Set<Long>> adyacencias = new HashMap<>(); // Declarar lista de adyacencia.

  public void agregarEspecialidad(Long id) {
    adyacencias.computeIfAbsent(id, ignored -> new HashSet<>()); // Crea un nuevo vértice sin conexiones iniciales.
  }

  public void relacionar(Long primeraId, Long segundaId) {
    if (primeraId.equals(segundaId)) // Valida que no cree un bucle consigo misma.
      throw new IllegalArgumentException("Una especialidad no puede relacionarse consigo misma");
    agregarEspecialidad(primeraId); // Asegura que ambos vértices existan.
    agregarEspecialidad(segundaId);
    adyacencias.get(primeraId).add(segundaId); // Grafo no dirigido, se agregan ambas direcciones. (A -> B y B -> A)
    adyacencias.get(segundaId).add(primeraId);
  }

  public Set<Long> relacionadasCon(Long id) {
    // getOrDefault evita excepciones | Set.copyof devuelve una copia inmutable.
    return Set.copyOf(adyacencias.getOrDefault(id, Set.of()));
  }

  // Algoritmo Principal (BFS).
  public List<Long> rutaMasCorta(Long origen, Long destino) {
    // Devuelve una lista vacía si alguno de los vértices no existe en el grafo.
    if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino))
      return List.of();
    Queue<Long> pendientes = new ArrayDeque<>();
    Map<Long, Long> anterior = new HashMap<>();
    Set<Long> visitadas = new HashSet<>();
    // Se agrega el origen a la cola de pendientes y al conjunto de visitadas.
    pendientes.add(origen);
    visitadas.add(origen);
    while (!pendientes.isEmpty()) { // Bucle de exploración BFS.
      Long actual = pendientes.remove(); // Extrae el vértice actual de la cola de pendientes.
      if (actual.equals(destino)) // Si el vértice actual es el destino, se ha encontrado la ruta más corta y se
        break; // rompe el bucle.
      for (Long vecino : adyacencias.get(actual)) // Si no es el destino, itera sobre todos sus vecinos.
        if (visitadas.add(vecino)) { // Si el vecino no ha sido visitado, se marca como visitado y se agrega a la
                                     // cola de pendientes.
          anterior.put(vecino, actual); // Al ser nuevo, se registra su origen y lo encola en pendientes.
          pendientes.add(vecino);
        }
    }
    if (!visitadas.contains(destino)) // Si el destino no fue alcanzado, no hay ruta posible.
      return List.of();
    LinkedList<Long> ruta = new LinkedList<>(); // Reconstrucción del camino.
    // Comienza desde el destino y retrocede hasta el origen.
    for (Long actual = destino; actual != null; actual = anterior.get(actual))
      ruta.addFirst(actual); // agrega cada paso al inicio.
    return List.copyOf(ruta);
  }

  // Métodos de soporte
  public boolean existeConexion(Long primeraId, Long segundaId) { // Consulta en la tabla hash de adyacencias.
    Set<Long> ady = adyacencias.get(primeraId); // Obtiene la lista de adyacencia del primer vértice.
    return ady != null && ady.contains(segundaId); // Verifica si existe una conexión entre dos vértices.
  }

  public Set<Long> obtenerVertices() {
    return Set.copyOf(adyacencias.keySet()); // Devuelve un conjunto inmutable de los vértices del grafo.
  }

  public void limpiar() {
    adyacencias.clear();
  }
}
