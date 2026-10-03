package com.utp.clinitech.dao;

import java.time.OffsetDateTime; // Usado para manejar fechas y horas de las citas con zona horaria.
import java.util.List; // Usado para retornar listas de citas en consultas personalizadas.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para heredar operaciones CRUD y paginación de Spring Data JPA.
import com.utp.clinitech.model.Cita; // Usado para representar la entidad Cita gestionada por este repositorio.

// Repositorio de acceso a datos (DAO) para la entidad Cita médica.
public interface CitaDAO extends JpaRepository<Cita, Long> {
    // Verifica si ya existe una cita agendada para un médico en una fecha y hora específica.
    boolean existsByMedicoIdAndFechaHora(Long medicoId, OffsetDateTime fechaHora);

    // Obtiene las citas de un médico en un rango de fechas ordenadas cronológicamente (usado para la cola de atención).
    List<Cita> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(Long medicoId, OffsetDateTime inicio,
            OffsetDateTime fin);

    // Obtiene el historial de citas de un paciente ordenadas de la más reciente a la más antigua.
    List<Cita> findByPacienteIdOrderByFechaHoraDesc(Long pacienteId);

    // Obtiene el historial de citas de un médico ordenadas de la más reciente a la más antigua.
    List<Cita> findByMedicoIdOrderByFechaHoraDesc(Long medicoId);

    // Verifica si un médico tiene o ha tenido citas con un paciente determinado.
    boolean existsByMedicoIdAndPacienteId(Long medicoId, Long pacienteId);

    // Cuenta el número total de citas agendadas dentro de un rango de tiempo.
    long countByFechaHoraBetween(OffsetDateTime inicio, OffsetDateTime fin);

    // Cuenta las citas filtradas por su estado (ej. ATENDIDA, PENDIENTE) dentro de un rango de tiempo.
    long countByEstadoAndFechaHoraBetween(com.utp.clinitech.model.enums.EstadoCita estado, OffsetDateTime inicio,
            OffsetDateTime fin);

    // Lista todas las citas existentes en un intervalo de tiempo.
    List<Cita> findByFechaHoraBetween(OffsetDateTime inicio, OffsetDateTime fin);

    // Obtiene las últimas 5 citas registradas en un rango de tiempo para resúmenes o dashboards.
    List<Cita> findTop5ByFechaHoraBetweenOrderByFechaHoraDesc(OffsetDateTime inicio, OffsetDateTime fin);
}
