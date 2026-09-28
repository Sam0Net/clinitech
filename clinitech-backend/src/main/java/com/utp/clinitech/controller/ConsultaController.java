package com.utp.clinitech.controller;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.service.ConsultaService;
@RestController @RequestMapping("/api/consultas")
public class ConsultaController {
  private final ConsultaService service;
  public ConsultaController(ConsultaService service) { this.service = service; }
  @GetMapping public List<ConsultaResponse> historial(@RequestParam Long pacienteId) { return service.historial(pacienteId); }
  @PostMapping public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request)); }
}
