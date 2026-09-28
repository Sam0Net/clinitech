package com.utp.clinitech.util;

import java.util.*;

/** Undirected graph of related specialties, useful for recommendations and referrals. */
public class GrafoEspecialidades {
  private final Map<Long, Set<Long>> adyacencias = new HashMap<>();
  public void agregarEspecialidad(Long id) { adyacencias.computeIfAbsent(id, ignored -> new HashSet<>()); }
  public void relacionar(Long primeraId, Long segundaId) {
    if (primeraId.equals(segundaId)) throw new IllegalArgumentException("Una especialidad no puede relacionarse consigo misma");
    agregarEspecialidad(primeraId); agregarEspecialidad(segundaId); adyacencias.get(primeraId).add(segundaId); adyacencias.get(segundaId).add(primeraId);
  }
  public Set<Long> relacionadasCon(Long id) { return Set.copyOf(adyacencias.getOrDefault(id, Set.of())); }
  public List<Long> rutaMasCorta(Long origen, Long destino) {
    if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino)) return List.of();
    Queue<Long> pendientes = new ArrayDeque<>(); Map<Long, Long> anterior = new HashMap<>(); Set<Long> visitadas = new HashSet<>();
    pendientes.add(origen); visitadas.add(origen);
    while (!pendientes.isEmpty()) { Long actual = pendientes.remove(); if (actual.equals(destino)) break; for (Long vecino : adyacencias.get(actual)) if (visitadas.add(vecino)) { anterior.put(vecino, actual); pendientes.add(vecino); } }
    if (!visitadas.contains(destino)) return List.of();
    LinkedList<Long> ruta = new LinkedList<>(); for (Long actual = destino; actual != null; actual = anterior.get(actual)) ruta.addFirst(actual); return List.copyOf(ruta);
  }

  public boolean existeConexion(Long primeraId, Long segundaId) {
    Set<Long> ady = adyacencias.get(primeraId);
    return ady != null && ady.contains(segundaId);
  }

  public Set<Long> obtenerVertices() {
    return Set.copyOf(adyacencias.keySet());
  }

  public void limpiar() {
    adyacencias.clear();
  }
}
