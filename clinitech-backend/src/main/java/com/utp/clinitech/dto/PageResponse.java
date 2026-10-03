package com.utp.clinitech.dto;

import java.util.List; // Usado para contener la lista de elementos correspondientes a la página actual.
import org.springframework.data.domain.Page; // Usado para transformar el objeto Page de Spring Data al formato de respuesta JSON.

// DTO genérico para estandarizar las respuestas paginadas de la API con metadatos de navegación.
public record PageResponse<T>(
    List<T> content, 
    int page, 
    int size, 
    long totalElements, 
    int totalPages) {

  // Método fábrica estático que construye un PageResponse a partir de un Page de Spring Data JPA.
  public static <T> PageResponse<T> from(Page<T> page) { 
    return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()); 
  }
}
