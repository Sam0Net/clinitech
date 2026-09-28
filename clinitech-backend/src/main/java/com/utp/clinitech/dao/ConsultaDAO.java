package com.utp.clinitech.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.clinitech.model.Consulta;

public interface ConsultaDAO extends JpaRepository<Consulta, Long> {
  boolean existsByCitaId(Long citaId);

  List<Consulta> findByCitaPacienteIdOrderByFechaAtencionDesc(Long pacienteId);
}
