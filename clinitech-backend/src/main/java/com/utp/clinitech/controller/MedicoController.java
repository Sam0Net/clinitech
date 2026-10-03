package com.utp.clinitech.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.utp.clinitech.dto.MedicoRequest;
import com.utp.clinitech.dto.MedicoResponse;
import com.utp.clinitech.dto.PageResponse;
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
