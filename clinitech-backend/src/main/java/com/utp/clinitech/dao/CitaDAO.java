package com.utp.clinitech.dao;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.clinitech.model.Cita;

public interface CitaDAO extends JpaRepository<Cita, Long> {
  boolean existsByMedicoIdAndFechaHora(Long medicoId, OffsetDateTime fechaHora);

  List<Cita> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(Long medicoId, OffsetDateTime inicio,
      OffsetDateTime fin);

  List<Cita> findByPacienteIdOrderByFechaHoraDesc(Long pacienteId);

  List<Cita> findByMedicoIdOrderByFechaHoraDesc(Long medicoId);

  boolean existsByMedicoIdAndPacienteId(Long medicoId, Long pacienteId);

  long countByFechaHoraBetween(OffsetDateTime inicio, OffsetDateTime fin);

  long countByEstadoAndFechaHoraBetween(com.utp.clinitech.model.enums.EstadoCita estado, OffsetDateTime inicio,
      OffsetDateTime fin);

  List<Cita> findByFechaHoraBetween(OffsetDateTime inicio, OffsetDateTime fin);

  List<Cita> findTop5ByFechaHoraBetweenOrderByFechaHoraDesc(OffsetDateTime inicio, OffsetDateTime fin);
}
