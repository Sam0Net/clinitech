package com.utp.clinitech.controller;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.utp.clinitech.dto.ReporteAdministrativoResponse;
import com.utp.clinitech.service.ReporteService;
@RestController @RequestMapping("/api/reportes")
public class ReporteController {
  private final ReporteService service;
  public ReporteController(ReporteService service) { this.service = service; }
  @GetMapping("/administrativo") public ReporteAdministrativoResponse resumen(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) { return service.resumen(desde, hasta); }
}
