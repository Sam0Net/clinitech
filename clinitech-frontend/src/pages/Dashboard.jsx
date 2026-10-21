import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { api } from '../api';

export default function Dashboard() {
  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  if (user.role === 'PACIENTE') {
    return <Navigate to="/appointments" replace />;
  }
  const isMedico = user.role === 'MEDICO';
  const isAdminOrRecepcion = user.role === 'ADMIN' || user.role === 'RECEPCIONISTA';

  // Estados generales
  const [citas, setCitas] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [medicoSeleccionado, setMedicoSeleccionado] = useState(user.medicoId || '');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Cola de Triaje
  const [colaTriaje, setColaTriaje] = useState([]);
  const [loadingCola, setLoadingCola] = useState(false);
  const [llamando, setLlamando] = useState(false);

  // Llamar al Siguiente Paciente y Atender
  const [citaActiva, setCitaActiva] = useState(null);
  const [diagnostico, setDiagnostico] = useState('');
  const [tratamiento, setTratamiento] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [guardandoConsulta, setGuardandoConsulta] = useState(false);

  const cargarDatosGenerales = async () => {
    try {
      setLoading(true);
      setError('');
      const [citasData, medicosData] = await Promise.all([
        api('/api/citas'),
        api('/api/medicos'),
      ]);
      const listCitas = Array.isArray(citasData) ? citasData : (citasData?.content || []);
      const listMedicos = Array.isArray(medicosData) ? medicosData : (medicosData?.content || []);
      setCitas(listCitas);
      setMedicos(listMedicos);

      if (!isMedico && !medicoSeleccionado && listMedicos.length > 0) {
        setMedicoSeleccionado(listMedicos[0].id);
      }
    } catch (err) {
      setError(err.message || 'Error al sincronizar datos generales.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatosGenerales();
  }, []);

  const cargarColaTriaje = async (idMedico) => {
    if (!idMedico) return;
    setLoadingCola(true);
    try {
      const hoy = new Date().toISOString().split('T')[0];
      const data = await api(`/api/citas/cola-atencion?medicoId=${idMedico}&fecha=${hoy}`);
      setColaTriaje(Array.isArray(data) ? data : []);
    } catch {
      setColaTriaje([]);
    } finally {
      setLoadingCola(false);
    }
  };

  useEffect(() => {
    if (isMedico && user.medicoId) {
      cargarColaTriaje(user.medicoId);
    } else if (medicoSeleccionado) {
      cargarColaTriaje(medicoSeleccionado);
    }
  }, [medicoSeleccionado, isMedico, user.medicoId]);

  const handleLlamarSiguiente = async () => {
    const medId = isMedico ? user.medicoId : medicoSeleccionado;
    if (!medId) {
      setError('Por favor, selecciona un médico asignado para llamar al siguiente paciente.');
      return;
    }
    setError('');
    setSuccess('');
    setLlamando(true);
    try {
      const hoy = new Date().toISOString().split('T')[0];
      const siguiente = await api(`/api/citas/cola-atencion/siguiente?medicoId=${medId}&fecha=${hoy}`);
      setCitaActiva(siguiente);
      setSuccess(`Llamando al paciente: ${siguiente.pacienteNombre} (Prioridad: ${siguiente.prioridad})`);
    } catch (err) {
      setError(err.message || 'No hay más pacientes pendientes en espera para este médico hoy.');
    } finally {
      setLlamando(false);
    }
  };

  const handleFinalizarAtencion = async (e) => {
    e.preventDefault();
    if (!citaActiva) return;
    setGuardandoConsulta(true);
    setError('');
    setSuccess('');
    try {
      await api('/api/consultas', {
        method: 'POST',
        body: {
          citaId: citaActiva.id,
          diagnostico,
          tratamiento,
          observaciones,
        },
      });
      setSuccess(`Atención completada exitosamente para ${citaActiva.pacienteNombre}. Registro clínico guardado.`);
      setCitaActiva(null);
      setDiagnostico('');
      setTratamiento('');
      setObservaciones('');

      cargarDatosGenerales();
      const medId = isMedico ? user.medicoId : medicoSeleccionado;
      if (medId) cargarColaTriaje(medId);
    } catch (err) {
      setError(err.message || 'No se pudo registrar la consulta médica.');
    } finally {
      setGuardandoConsulta(false);
    }
  };

  const total = citas.length;
  const urgentes = citas.filter((c) => c.prioridad === 'URGENTE').length;
  const completadas = citas.filter((c) => c.estado === 'COMPLETADA' || c.estado === 'ATENDIDA').length;

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado y Acción de Actualización */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            {isMedico ? 'Consultorio y Atención Médica' : 'Panel de Control y Triaje'}
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            {isMedico
              ? 'Gestión prioritaria de pacientes en espera y registro de atenciones clínicas.'
              : 'Monitoreo hospitalario en tiempo real, métricas generales y estado de triaje.'}
          </p>
        </div>

        <button
          onClick={() => {
            cargarDatosGenerales();
            const medId = isMedico ? user.medicoId : medicoSeleccionado;
            if (medId) cargarColaTriaje(medId);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-white border border-slate-300 text-slate-700 hover:text-[#006591] hover:border-[#006591] hover:bg-slate-50 font-semibold text-xs rounded-xl shadow-xs transition-all self-start min-h-[44px]"
          aria-label="Actualizar datos clínicos"
        >
          <span className="material-symbols-outlined text-[18px]">refresh</span>
          Actualizar Datos
        </button>
      </div>

      {/* Alertas Accesibles (WCAG Live Regions) */}
      {error && (
        <div
          role="alert"
          aria-live="polite"
          className="p-4 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl flex items-start gap-3 shadow-xs"
        >
          <span className="material-symbols-outlined text-rose-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            error
          </span>
          <div className="text-xs text-rose-800 font-medium">{error}</div>
        </div>
      )}

      {success && (
        <div
          role="alert"
          aria-live="polite"
          className="p-4 bg-emerald-50 border-l-4 border-emerald-600 rounded-r-xl flex items-start gap-3 shadow-xs"
        >
          <span className="material-symbols-outlined text-emerald-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            check_circle
          </span>
          <div className="text-xs text-emerald-800 font-medium">{success}</div>
        </div>
      )}

      {/* Tarjetas de Métricas Generales (UI / UX Architect) */}
      <section aria-label="Métricas del día" className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <div className="bg-white rounded-2xl p-6 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Citas Programadas</span>
            <div className="w-11 h-11 rounded-xl bg-sky-50 text-[#006591] flex items-center justify-center">
              <span className="material-symbols-outlined text-[24px]">calendar_month</span>
            </div>
          </div>
          <p className="text-3xl sm:text-4xl font-extrabold text-slate-900 mt-3">{loading ? '...' : total}</p>
          <p className="text-xs text-slate-500 mt-1">Registradas en el centro clínico</p>
        </div>

        <div className="bg-white rounded-2xl p-6 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-rose-700">Casos Urgentes</span>
            <div className="w-11 h-11 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center">
              <span className="material-symbols-outlined text-[24px]">emergency</span>
            </div>
          </div>
          <p className="text-3xl sm:text-4xl font-extrabold text-rose-700 mt-3">{loading ? '...' : urgentes}</p>
          <p className="text-xs text-rose-600 font-medium mt-1">Atención inmediata requerida</p>
        </div>

        <div className="bg-white rounded-2xl p-6 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-emerald-700">Atenciones Realizadas</span>
            <div className="w-11 h-11 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <span className="material-symbols-outlined text-[24px]">verified</span>
            </div>
          </div>
          <p className="text-3xl sm:text-4xl font-extrabold text-emerald-700 mt-3">{loading ? '...' : completadas}</p>
          <p className="text-xs text-emerald-600 font-medium mt-1">Consultas clínicas completadas</p>
        </div>
      </section>

      {/* Sección: Cola de Atención y Triaje del Día */}
      <section aria-labelledby="triaje-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 pb-5 border-b border-slate-200">
          <div>
            <h2 id="triaje-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2.5">
              <span className="material-symbols-outlined text-[#006591]">clinical_notes</span>
              Cola de Atención y Triaje del Día
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Ordenación automatizada por orden de llegada con máxima prioridad para casos de urgencia médica.
            </p>
          </div>

          <div className="flex flex-row items-center gap-2.5 sm:gap-3 flex-wrap sm:flex-nowrap shrink-0">
            {isAdminOrRecepcion && (
              <div className="flex items-center gap-2">
                <label htmlFor="select-medico-triaje" className="text-xs font-bold text-slate-700 whitespace-nowrap">
                  Médico:
                </label>
                <select
                  id="select-medico-triaje"
                  value={medicoSeleccionado}
                  onChange={(e) => setMedicoSeleccionado(e.target.value)}
                  className="px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-800 font-medium focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[42px] max-w-[260px]"
                >
                  <option value="">Selecciona médico a consultar...</option>
                  {medicos.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.nombreCompleto || `${m.nombres} ${m.apellidos}`} ({m.especialidad?.nombre || 'General'})
                    </option>
                  ))}
                </select>
              </div>
            )}

            <button
              onClick={handleLlamarSiguiente}
              disabled={llamando}
              className="shrink-0 bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-4 sm:px-5 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 shadow-sm transition-all min-h-[42px] whitespace-nowrap"
            >
              <span className="material-symbols-outlined text-[18px] sm:text-[20px]">record_voice_over</span>
              {llamando ? 'Llamando...' : 'Llamar Siguiente Paciente'}
            </button>
          </div>
        </div>

        {/* Modal / Ficha Activa de Consulta Médica */}
        {citaActiva && (
          <div
            role="region"
            aria-label="Ficha de atención activa"
            className="p-6 bg-gradient-to-br from-sky-50 to-blue-50/60 border-2 border-[#006591] rounded-2xl space-y-5 animate-in fade-in duration-150"
          >
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-4 border-b border-sky-200">
              <div>
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-[#006591] text-white text-[11px] font-extrabold rounded-full uppercase tracking-wider">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" aria-hidden="true" />
                  En Atención Clínica Activa
                </span>
                <h3 className="text-xl font-extrabold text-slate-900 mt-1.5">
                  {citaActiva.pacienteNombre}
                </h3>
                <p className="text-xs text-slate-600 mt-0.5">
                  <strong className="text-slate-800">Motivo:</strong> {citaActiva.motivoConsulta} |{' '}
                  <strong className="text-slate-800">Prioridad:</strong>{' '}
                  <span
                    className={`font-bold ${
                      citaActiva.prioridad === 'URGENTE' ? 'text-rose-700' : 'text-[#006591]'
                    }`}
                  >
                    {citaActiva.prioridad}
                  </span>
                </p>
              </div>

              <button
                type="button"
                onClick={() => setCitaActiva(null)}
                className="px-3 py-1.5 text-xs text-slate-600 hover:text-slate-900 font-semibold bg-white/80 hover:bg-white rounded-lg border border-slate-300 self-start"
              >
                Cerrar ficha
              </button>
            </div>

            <form onSubmit={handleFinalizarAtencion} className="space-y-4">
              <div>
                <label htmlFor="diagnostico-input" className="text-xs font-bold text-slate-800 block mb-1">
                  Diagnóstico Clínico <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="diagnostico-input"
                  type="text"
                  required
                  placeholder="Ej. Faringitis aguda bacteriana, evaluación cardiopulmonar estable..."
                  value={diagnostico}
                  onChange={(e) => setDiagnostico(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div>
                <label htmlFor="tratamiento-input" className="text-xs font-bold text-slate-800 block mb-1">
                  Tratamiento y Receta Médica <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <textarea
                  id="tratamiento-input"
                  required
                  rows={3}
                  placeholder="Medicamentos recetados, posología, indicaciones de reposo..."
                  value={tratamiento}
                  onChange={(e) => setTratamiento(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div>
                <label htmlFor="observaciones-input" className="text-xs font-bold text-slate-800 block mb-1">
                  Observaciones Adicionales
                </label>
                <input
                  id="observaciones-input"
                  type="text"
                  placeholder="Próximo control sugerido, derivación recomendada, signos de alarma..."
                  value={observaciones}
                  onChange={(e) => setObservaciones(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setCitaActiva(null)}
                  className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-100 rounded-xl text-xs font-semibold min-h-[40px]"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={guardandoConsulta}
                  className="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-60 text-white px-5 py-2.5 rounded-xl text-xs font-bold shadow-sm flex items-center gap-2 min-h-[40px]"
                >
                  <span className="material-symbols-outlined text-[18px]">verified</span>
                  {guardandoConsulta ? 'Guardando consulta...' : 'Finalizar Consulta Médica'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Tabla Accesible de la Cola de Pacientes */}
        <div className="overflow-x-auto rounded-xl border border-slate-200">
          <table className="w-full text-left text-sm border-collapse">
            <caption className="sr-only">Cola de espera y triaje de pacientes para el día de hoy</caption>
            <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
              <tr>
                <th scope="col" className="py-3 px-4">Orden de Atención</th>
                <th scope="col" className="py-3 px-4">Paciente</th>
                <th scope="col" className="py-3 px-4">Horario</th>
                <th scope="col" className="py-3 px-4">Nivel de Prioridad</th>
                <th scope="col" className="py-3 px-4">Motivo de Consulta</th>
                <th scope="col" className="py-3 px-4 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loadingCola ? (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-xs text-slate-500">
                    <div className="flex items-center justify-center gap-2">
                      <span className="material-symbols-outlined animate-spin text-[#006591]">progress_activity</span>
                      Cargando lista de pacientes en espera...
                    </div>
                  </td>
                </tr>
              ) : colaTriaje.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-10 text-center text-xs text-slate-500">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <span className="material-symbols-outlined text-3xl text-slate-400">check_box</span>
                      <p className="font-semibold text-slate-700">No hay pacientes pendientes en espera para este médico hoy.</p>
                      <p className="text-slate-500 text-[11px]">Todos los pacientes del turno han sido atendidos o aún no registran llegada.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                colaTriaje.map((c, index) => (
                  <tr
                    key={c.id}
                    className={`transition-colors ${
                      index === 0 ? 'bg-sky-50/70 hover:bg-sky-50 font-medium' : 'hover:bg-slate-50'
                    }`}
                  >
                    <td className="py-3.5 px-4 font-bold text-xs text-[#006591]">
                      {index === 0 ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-sky-100 text-[#006591] font-extrabold text-[11px]">
                          ⭐ Siguiente
                        </span>
                      ) : (
                        `#${index + 1}`
                      )}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-slate-900">{c.pacienteNombre}</td>
                    <td className="py-3.5 px-4 text-xs font-medium text-slate-600">
                      {new Date(c.fechaHora).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                          c.prioridad === 'URGENTE'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-sky-100 text-[#006591]'
                        }`}
                      >
                        <span
                          className={`w-1.5 h-1.5 rounded-full ${
                            c.prioridad === 'URGENTE' ? 'bg-rose-600' : 'bg-[#006591]'
                          }`}
                          aria-hidden="true"
                        />
                        {c.prioridad}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-xs text-slate-600 max-w-[260px] truncate">
                      {c.motivoConsulta}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => {
                          setCitaActiva(c);
                          window.scrollTo({ top: 350, behavior: 'smooth' });
                        }}
                        aria-label={`Atender consulta de ${c.pacienteNombre}`}
                        className="px-3.5 py-1.5 text-xs bg-[#006591] text-white hover:bg-[#004d70] rounded-xl font-bold transition-all shadow-2xs min-h-[36px]"
                      >
                        Atender
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* Historial General de Citas */}
      <section aria-labelledby="todas-citas-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-200 flex items-center justify-between">
          <div>
            <h2 id="todas-citas-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <span className="material-symbols-outlined text-[#006591]">history</span>
              Todas las Citas del Sistema
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">Registro completo de citas programadas, atendidas y canceladas.</p>
          </div>
          <span className="text-xs font-bold text-slate-600 px-3 py-1 bg-slate-100 rounded-full">
            Total: {citas.length}
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <caption className="sr-only">Listado general de todas las citas del sistema</caption>
            <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
              <tr>
                <th scope="col" className="py-3 px-4">ID</th>
                <th scope="col" className="py-3 px-4">Paciente</th>
                <th scope="col" className="py-3 px-4">Médico</th>
                <th scope="col" className="py-3 px-4">Fecha y Hora</th>
                <th scope="col" className="py-3 px-4">Prioridad</th>
                <th scope="col" className="py-3 px-4">Estado</th>
                <th scope="col" className="py-3 px-4">Motivo</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-xs text-slate-500">
                    Cargando citas...
                  </td>
                </tr>
              ) : citas.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-xs text-slate-500">
                    No hay citas registradas en la base de datos actualmente.
                  </td>
                </tr>
              ) : (
                citas.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-xs text-slate-500">#{c.id}</td>
                    <td className="py-3.5 px-4 font-bold text-slate-900">{c.pacienteNombre}</td>
                    <td className="py-3.5 px-4 text-xs font-medium text-slate-700">{c.medicoNombre}</td>
                    <td className="py-3.5 px-4 text-xs text-slate-600">
                      {new Date(c.fechaHora).toLocaleString('es-PE', {
                        dateStyle: 'short',
                        timeStyle: 'short',
                      })}
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                          c.prioridad === 'URGENTE'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-sky-100 text-[#006591]'
                        }`}
                      >
                        {c.prioridad}
                      </span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                          c.estado === 'COMPLETADA' || c.estado === 'ATENDIDA'
                            ? 'bg-emerald-100 text-emerald-800'
                            : c.estado === 'CONFIRMADA'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        {c.estado}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-xs text-slate-600 max-w-[220px] truncate">
                      {c.motivoConsulta}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}