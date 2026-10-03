package com.utp.clinitech.dao;

import java.util.List; // Usado para retornar listas de consultas médicas en el historial del paciente.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para operaciones CRUD y persistencia JPA sobre consultas médicas.
import com.utp.clinitech.model.Consulta; // Usado para representar la entidad Consulta clínica en la base de datos.

// Repositorio de acceso a datos (DAO) para la entidad Consulta médica.
public interface ConsultaDAO extends JpaRepository<Consulta, Long> {
  // Comprueba si una cita médica ya tiene una consulta clínica registrada para evitar duplicados.
  boolean existsByCitaId(Long citaId);

  // Obtiene el historial de consultas médicas de un paciente ordenadas cronológicamente de forma descendente.
  List<Consulta> findByCitaPacienteIdOrderByFechaAtencionDesc(Long pacienteId);
}
