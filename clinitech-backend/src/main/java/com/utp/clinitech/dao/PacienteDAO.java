package com.utp.clinitech.dao;

import java.util.Optional; // Usado para encapsular el resultado de búsqueda de paciente de forma segura evitando null.
import org.springframework.data.domain.Page; // Usado para encapsular la página de pacientes resultante.
import org.springframework.data.domain.Pageable; // Usado para gestionar la paginación y ordenamiento de pacientes.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para operaciones CRUD de persistencia sobre pacientes.
import org.springframework.data.jpa.repository.Query; // Usado para definir consultas JPQL personalizadas de búsqueda.
import org.springframework.data.repository.query.Param; // Usado para mapear el término de búsqueda a la consulta JPQL.
import com.utp.clinitech.model.Paciente; // Usado para representar la entidad Paciente en la base de datos.

// Repositorio de acceso a datos (DAO) para los pacientes de la clínica.
public interface PacienteDAO extends JpaRepository<Paciente, Long> {
  // Busca un paciente por su número único de DNI.
  Optional<Paciente> findByDni(String dni);

  // Retorna el conteo total de pacientes activos en el sistema para reportes administrativos.
  long countByActivoTrue();

  // Búsqueda paginada de pacientes activos por coincidencias en nombres, apellidos o DNI.
  @Query("select p from Paciente p where p.activo = true and (lower(p.nombres) like lower(concat('%', :term, '%')) or lower(p.apellidos) like lower(concat('%', :term, '%')) or p.dni like concat('%', :term, '%'))")
  Page<Paciente> buscarActivos(@Param("term") String term, Pageable pageable);
}
