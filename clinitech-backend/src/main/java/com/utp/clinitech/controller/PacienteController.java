package com.utp.clinitech.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.service.PacienteService;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
  private final PacienteService service;
  public PacienteController(PacienteService service) { this.service = service; }
  @GetMapping public PageResponse<PacienteResponse> listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.listar(q, Math.max(0, page), Math.max(1, size)); }
  @GetMapping("/arbol/buscar/{dni}") public PacienteResponse buscarEnArbol(@PathVariable String dni) { return service.buscarPorDniEnArbol(dni); }
  @GetMapping("/arbol/en-orden") public java.util.List<PacienteResponse> listarEnOrdenArbol() { return service.listarEnOrdenArbol(); }
  @GetMapping("/{id}") public PacienteResponse obtener(@PathVariable Long id) { return service.obtener(id); }
  @PostMapping public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) { PacienteResponse created = service.crear(request); return ResponseEntity.status(HttpStatus.CREATED).body(created); }
  @PutMapping("/{id}") public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) { return service.actualizar(id, request); }
  @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void desactivar(@PathVariable Long id) { service.desactivar(id); }
}
