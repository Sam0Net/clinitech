package com.utp.clinitech.controller;

import java.time.LocalDate; // Usado para recibir las fechas límite de inicio y fin del reporte.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear peticiones HTTP GET al resumen administrativo.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para establecer la ruta base de reportes (/api/reportes).
import org.springframework.web.bind.annotation.RequestParam; // Usado para capturar los parámetros de rango de fechas de la URL.
import org.springframework.web.bind.annotation.RestController; // Usado para marcar la clase como controlador REST.
import com.utp.clinitech.dto.ReporteAdministrativoResponse; // Usado para retornar el consolidado de métricas y estadísticas.
import com.utp.clinitech.service.ReporteService; // Usado para procesar y calcular los indicadores hospitalarios.

// Controlador REST para la generación de reportes y métricas estadísticas administrativas.
@RestController 
@RequestMapping("/api/reportes")
public class ReporteController {
  private final ReporteService service;

  // Constructor con inyección del servicio de reportes.
  public ReporteController(ReporteService service) { 
    this.service = service; 
  }

  // Endpoint para obtener el reporte administrativo y ranking de médicos dentro de un período de fechas.
  @GetMapping("/administrativo") 
  public ReporteAdministrativoResponse resumen(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) { 
    return service.resumen(desde, hasta); 
  }
}
