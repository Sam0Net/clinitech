package com.utp.clinitech.dao;

import java.util.List; // Usado para devolver colecciones de especialidades médicas activas.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para operaciones de persistencia JPA sobre las especialidades.
import com.utp.clinitech.model.Especialidad; // Usado para representar la entidad Especialidad en la base de datos.

// Repositorio de acceso a datos (DAO) para la entidad Especialidad médica.
public interface EspecialidadDAO extends JpaRepository<Especialidad, Long> {
  // Lista todas las especialidades médicas activas ordenadas alfabéticamente por su nombre.
  List<Especialidad> findByActivoTrueOrderByNombreAsc();
}
