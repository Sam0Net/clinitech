package com.utp.clinitech.dao;

import java.util.List; // Usado para retornar listas de médicos pertenecientes a una especialidad.
import org.springframework.data.domain.Page; // Usado para encapsular el resultado de médicos paginado.
import org.springframework.data.domain.Pageable; // Usado para recibir parámetros de página, tamaño y ordenamiento.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para operaciones CRUD en la persistencia de médicos.
import org.springframework.data.jpa.repository.Query; // Usado para definir consultas personalizadas con JPQL.
import org.springframework.data.repository.query.Param; // Usado para vincular parámetros en la consulta JPQL.
import com.utp.clinitech.model.Medico; // Usado para representar la entidad Medico en la base de datos.

// Repositorio de acceso a datos (DAO) para el personal médico.
public interface MedicoDAO extends JpaRepository<Medico, Long> {
  // Obtiene los médicos activos asociados a una especialidad ordenados alfabéticamente por apellidos.
  List<Medico> findByActivoTrueAndEspecialidadIdOrderByApellidosAsc(Long especialidadId);

  // Búsqueda paginada de médicos activos por coincidencia de nombres, apellidos o número de colegiatura (CMP).
  @Query("select m from Medico m where m.activo = true and (lower(m.nombres) like lower(concat('%', :term, '%')) or lower(m.apellidos) like lower(concat('%', :term, '%')) or lower(m.cmp) like lower(concat('%', :term, '%')))")
  Page<Medico> buscarActivos(@Param("term") String term, Pageable pageable);

  // Cuenta el total de médicos actualmente activos en la clínica para métricas y reportes.
  long countByActivoTrue();
}
