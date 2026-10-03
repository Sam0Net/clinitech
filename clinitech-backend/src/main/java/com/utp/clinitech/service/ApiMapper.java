package com.utp.clinitech.service;

import com.utp.clinitech.dto.CitaResponse; // Usado para transferir datos mapeados de citas al cliente.
import com.utp.clinitech.dto.ConsultaResponse; // Usado para transferir datos mapeados de consultas médicas al cliente.
import com.utp.clinitech.dto.EspecialidadResponse; // Usado para transferir datos mapeados de especialidades al cliente.
import com.utp.clinitech.dto.MedicoResponse; // Usado para transferir datos mapeados de médicos al cliente.
import com.utp.clinitech.dto.PacienteResponse; // Usado para transferir datos mapeados de pacientes al cliente.
import com.utp.clinitech.model.Cita; // Usado como entidad fuente para mapear información de citas.
import com.utp.clinitech.model.Consulta; // Usado como entidad fuente para mapear información de consultas.
import com.utp.clinitech.model.Especialidad; // Usado como entidad fuente para mapear información de especialidades.
import com.utp.clinitech.model.Medico; // Usado como entidad fuente para mapear información de médicos.
import com.utp.clinitech.model.Paciente; // Usado como entidad fuente para mapear información de pacientes.

// Clase utilitaria estática para transformar entidades JPA en DTOs de respuesta seguros para la API.
final class ApiMapper {
  private ApiMapper() {
  }

  // Transforma una entidad Paciente en su DTO PacienteResponse correspondiente.
  static PacienteResponse paciente(Paciente p) {
    return new PacienteResponse(p.getId(), p.getDni(), p.getNombres(), p.getApellidos(), p.getFechaNacimiento(),
        p.getTelefono(), p.getCorreo(), p.getDireccion(), p.getAlergias(), p.getHistorialMedico(), p.isActivo());
  }

  // Transforma una entidad Especialidad en su DTO EspecialidadResponse.
  static EspecialidadResponse especialidad(Especialidad e) {
    return new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.isActivo());
  }

  // Transforma una entidad Medico en su DTO MedicoResponse, incluyendo especialidad y nombre completo.
  static MedicoResponse medico(Medico m) {
    return new MedicoResponse(m.getId(), m.getNombres(), m.getApellidos(), m.nombreCompleto(), m.getCmp(),
        especialidad(m.getEspecialidad()), m.getTelefono(), m.getCorreo(), m.getHorarioAtencion(), m.isActivo());
  }

  // Transforma una entidad Cita en su DTO CitaResponse con nombres enriquecidos de médico y paciente.
  static CitaResponse cita(Cita c) {
    return new CitaResponse(c.getId(), c.getPaciente().getId(), c.getPaciente().nombreCompleto(), c.getMedico().getId(),
        c.getMedico().nombreCompleto(), c.getFechaHora(), c.getEstado(), c.getPrioridad(), c.getMotivoConsulta());
  }

  // Transforma una entidad Consulta en su DTO ConsultaResponse con datos de la cita y el médico.
  static ConsultaResponse consulta(Consulta c) {
    return new ConsultaResponse(c.getId(), c.getCita().getId(), c.getCita().getPaciente().getId(),
        c.getCita().getMedico().nombreCompleto(), c.getDiagnostico(), c.getTratamiento(), c.getObservaciones(),
        c.getFechaAtencion());
  }
}
