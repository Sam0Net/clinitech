package com.utp.clinitech.dao;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.utp.clinitech.model.Paciente;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {
  Optional<Paciente> findByDni(String dni);

  long countByActivoTrue();

  @Query("select p from Paciente p where p.activo = true and (lower(p.nombres) like lower(concat('%', :term, '%')) or lower(p.apellidos) like lower(concat('%', :term, '%')) or p.dni like concat('%', :term, '%'))")
  Page<Paciente> buscarActivos(@Param("term") String term, Pageable pageable);
}
