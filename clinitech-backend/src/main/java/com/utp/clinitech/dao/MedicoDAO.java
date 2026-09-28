package com.utp.clinitech.dao;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.utp.clinitech.model.Medico;

public interface MedicoDAO extends JpaRepository<Medico, Long> {
  List<Medico> findByActivoTrueAndEspecialidadIdOrderByApellidosAsc(Long especialidadId);

  @Query("select m from Medico m where m.activo = true and (lower(m.nombres) like lower(concat('%', :term, '%')) or lower(m.apellidos) like lower(concat('%', :term, '%')) or lower(m.cmp) like lower(concat('%', :term, '%')))")
  Page<Medico> buscarActivos(@Param("term") String term, Pageable pageable);

  long countByActivoTrue();
}
