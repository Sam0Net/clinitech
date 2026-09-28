package com.utp.clinitech.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.service.MedicoService;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
  private final MedicoService service;

  public MedicoController(MedicoService service) {
    this.service = service;
  }
  @GetMapping(params = "especialidadId")
  public List<MedicoResponse> porEspecialidad(@RequestParam Long especialidadId) {
    return service.porEspecialidad(especialidadId);
  }

  @GetMapping
  public PageResponse<MedicoResponse> listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.listar(q, Math.max(0, page), Math.max(1, size));
  }
  @PostMapping public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); }
  @PutMapping("/{id}") public MedicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest request) { return service.actualizar(id, request); }
  @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void desactivar(@PathVariable Long id) { service.desactivar(id); }
}
