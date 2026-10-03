package com.utp.clinitech.controller;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.utp.clinitech.dto.ConsultaRequest;
import com.utp.clinitech.dto.ConsultaResponse;
import com.utp.clinitech.service.ConsultaService;
@RestController @RequestMapping("/api/consultas")
public class ConsultaController {
  private final ConsultaService service;
  public ConsultaController(ConsultaService service) { this.service = service; }
  @GetMapping public List<ConsultaResponse> historial(@RequestParam Long pacienteId) { return service.historial(pacienteId); }
  @PostMapping public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request)); }
}
