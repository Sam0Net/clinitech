package com.utp.clinitech.controller;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.service.EspecialidadService;
@RestController @RequestMapping("/api/especialidades")
public class EspecialidadController {
  private final EspecialidadService service;
  public EspecialidadController(EspecialidadService service) { this.service = service; }
  @GetMapping public List<EspecialidadResponse> listar() { return service.listar(); }
  @GetMapping("/{id}/relacionadas") public List<EspecialidadResponse> relacionadas(@PathVariable Long id) { return service.relacionadas(id); }
  @GetMapping("/interconsultas/ruta") public List<EspecialidadResponse> rutaInterconsulta(@RequestParam Long origenId, @RequestParam Long destinoId) { return service.rutaInterconsulta(origenId, destinoId); }
  @PostMapping public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); }
}
