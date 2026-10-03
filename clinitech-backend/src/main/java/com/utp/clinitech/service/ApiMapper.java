package com.utp.clinitech.service;

import com.utp.clinitech.dto.CitaResponse;
import com.utp.clinitech.dto.ConsultaResponse;
import com.utp.clinitech.dto.EspecialidadResponse;
import com.utp.clinitech.dto.MedicoResponse;
import com.utp.clinitech.dto.PacienteResponse;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.Consulta;
import com.utp.clinitech.model.Especialidad;
import com.utp.clinitech.model.Medico;
import com.utp.clinitech.model.Paciente;

final class ApiMapper {
  private ApiMapper() {
  }

  static PacienteResponse paciente(Paciente p) {
    return new PacienteResponse(p.getId(), p.getDni(), p.getNombres(), p.getApellidos(), p.getFechaNacimiento(),
        p.getTelefono(), p.getCorreo(), p.getDireccion(), p.getAlergias(), p.getHistorialMedico(), p.isActivo());
  }

  static EspecialidadResponse especialidad(Especialidad e) {
    return new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.isActivo());
  }

  static MedicoResponse medico(Medico m) {
    return new MedicoResponse(m.getId(), m.getNombres(), m.getApellidos(), m.nombreCompleto(), m.getCmp(),
        especialidad(m.getEspecialidad()), m.getTelefono(), m.getCorreo(), m.getHorarioAtencion(), m.isActivo());
  }

  static CitaResponse cita(Cita c) {
    return new CitaResponse(c.getId(), c.getPaciente().getId(), c.getPaciente().nombreCompleto(), c.getMedico().getId(),
        c.getMedico().nombreCompleto(), c.getFechaHora(), c.getEstado(), c.getPrioridad(), c.getMotivoConsulta());
  }

  static ConsultaResponse consulta(Consulta c) {
    return new ConsultaResponse(c.getId(), c.getCita().getId(), c.getCita().getPaciente().getId(),
        c.getCita().getMedico().nombreCompleto(), c.getDiagnostico(), c.getTratamiento(), c.getObservaciones(),
        c.getFechaAtencion());
  }
}
