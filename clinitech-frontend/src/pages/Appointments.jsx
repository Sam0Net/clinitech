import { useEffect, useState } from 'react';
import { api } from '../api';

export default function Appointments() {
  const [citas, setCitas] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [pacientes, setPacientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showNuevoPaciente, setShowNuevoPaciente] = useState(false);
  const [guardandoCita, setGuardandoCita] = useState(false);

  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  const isPaciente = user.role === 'PACIENTE';

  // Filtro por especialidad
  const [especialidadFiltro, setEspecialidadFiltro] = useState('');

  // Formulario nueva cita
  const [form, setForm] = useState({
    pacienteId: '',
    medicoId: '',
    fecha: '',
    hora: '',
    prioridad: 'NORMAL',
    motivoConsulta: '',
  });

  // Horarios disponibles consultados desde el backend
  const [slotsDisponibles, setSlotsDisponibles] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  // Formulario nuevo paciente (para admisión)
  const [pacienteForm, setPacienteForm] = useState({
    dni: '',
    nombres: '',
    apellidos: '',
    fechaNacimiento: '',
    telefono: '',
    correo: '',
    direccion: '',
    alergias: '',
    historialMedico: '',
  });

  // Filtros de listado para Ventanilla y búsqueda rápida
  const [filtroTexto, setFiltroTexto] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('TODAS');
  const [filtroFecha, setFiltroFecha] = useState('TODAS'); // 'TODAS' | 'HOY' | 'FUTURAS'

  // Modal de Reprogramación de Citas
  const [showReprogramarModal, setShowReprogramarModal] = useState(false);
  const [citaParaReprogramar, setCitaParaReprogramar] = useState(null);
  const [reprogFecha, setReprogFecha] = useState('');
  const [reprogHora, setReprogHora] = useState('');
  const [slotsReprog, setSlotsReprog] = useState([]);
  const [loadingSlotsReprog, setLoadingSlotsReprog] = useState(false);
  const [guardandoReprog, setGuardandoReprog] = useState(false);
  const [reprogError, setReprogError] = useState('');

  const cargarDatos = async () => {
    try {
      setLoading(true);
      setError('');
      const [citasData, medicosData, espData, pacientesData] = await Promise.all([
        api('/api/citas'),
        api('/api/medicos'),
        api('/api/especialidades'),
        isPaciente
          ? Promise.resolve([])
          : api('/api/pacientes/arbol/en-orden').catch(() => api('/api/pacientes').then((p) => p?.content || [])),
      ]);
      setCitas(Array.isArray(citasData) ? citasData : (citasData?.content || []));
      setMedicos(Array.isArray(medicosData) ? medicosData : (medicosData?.content || []));
      setEspecialidades(Array.isArray(espData) ? espData : (espData?.content || []));
      setPacientes(Array.isArray(pacientesData) ? pacientesData : (pacientesData?.content || []));
    } catch (err) {
      setError(err.message || 'Error al cargar información de citas.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const consultarDisponibilidad = async (idMed, fch) => {
    if (!idMed || !fch) {
      setSlotsDisponibles([]);
      return;
    }
    setLoadingSlots(true);
    try {
      const slots = await api(`/api/citas/disponibilidad?medicoId=${idMed}&fecha=${fch}`);
      setSlotsDisponibles(Array.isArray(slots) ? slots : []);
    } catch {
      setSlotsDisponibles([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleMedicoChange = (id) => {
    setForm((prev) => ({ ...prev, medicoId: id, hora: '' }));
    if (form.fecha) consultarDisponibilidad(id, form.fecha);
  };

  const handleFechaChange = (f) => {
    setForm((prev) => ({ ...prev, fecha: f, hora: '' }));
    if (form.medicoId) consultarDisponibilidad(form.medicoId, f);
  };

  const medicosFiltrados = especialidadFiltro
    ? medicos.filter((m) => m.especialidad?.id === Number(especialidadFiltro))
    : medicos;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setGuardandoCita(true);
    try {
      const fechaHoraISO = form.hora || `${form.fecha}T09:00:00Z`;
      const payload = {
        pacienteId: isPaciente ? null : (form.pacienteId ? Number(form.pacienteId) : null),
        medicoId: Number(form.medicoId),
        fechaHora: new Date(fechaHoraISO).toISOString(),
        prioridad: form.prioridad,
        motivoConsulta: form.motivoConsulta,
      };
      await api('/api/citas', { method: 'POST', body: payload });
      setSuccess('Cita médica programada y confirmada exitosamente.');
      setForm({ pacienteId: '', medicoId: '', fecha: '', hora: '', prioridad: 'NORMAL', motivoConsulta: '' });
      setSlotsDisponibles([]);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'Error al programar la cita.');
    } finally {
      setGuardandoCita(false);
    }
  };

  const handleCrearPaciente = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const nuevo = await api('/api/pacientes', {
        method: 'POST',
        body: pacienteForm,
      });
      setSuccess(`Paciente ${nuevo.nombres} ${nuevo.apellidos} registrado y admitido exitosamente.`);
      setShowNuevoPaciente(false);
      setPacienteForm({
        dni: '',
        nombres: '',
        apellidos: '',
        fechaNacimiento: '',
        telefono: '',
        correo: '',
        direccion: '',
        alergias: '',
        historialMedico: '',
      });
      await cargarDatos();
      setForm((prev) => ({ ...prev, pacienteId: nuevo.id }));
    } catch (err) {
      setError(err.message || 'Error al registrar paciente.');
    }
  };

  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      await api(`/api/citas/${id}/estado`, {
        method: 'PATCH',
        body: { estado: nuevoEstado },
      });
      setSuccess(`Estado de la cita #${id} actualizado a ${nuevoEstado}.`);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'Error al actualizar estado.');
    }
  };

  const handleConfirmarLlegada = async (id, pacienteNombre) => {
    try {
      await api(`/api/citas/${id}/estado`, {
        method: 'PATCH',
        body: { estado: 'CONFIRMADA' },
      });
      setSuccess(`Check-in confirmado para ${pacienteNombre}. El paciente ingresó a la sala de espera.`);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'Error al confirmar llegada del paciente.');
    }
  };

  const abrirModalReprogramar = (cita) => {
    setCitaParaReprogramar(cita);
    setReprogFecha('');
    setReprogHora('');
    setSlotsReprog([]);
    setReprogError('');
    setShowReprogramarModal(true);
  };

  const handleReprogFechaChange = async (fecha) => {
    setReprogFecha(fecha);
    setReprogHora('');
    setReprogError('');
    if (!citaParaReprogramar?.medicoId || !fecha) {
      setSlotsReprog([]);
      return;
    }
    setLoadingSlotsReprog(true);
    try {
      const slots = await api(`/api/citas/disponibilidad?medicoId=${citaParaReprogramar.medicoId}&fecha=${fecha}`);
      setSlotsReprog(Array.isArray(slots) ? slots : []);
    } catch {
      setSlotsReprog([]);
      setReprogError('No se pudieron consultar los horarios disponibles para esta fecha.');
    } finally {
      setLoadingSlotsReprog(false);
    }
  };

  const handleConfirmarReprogramacion = async (e) => {
    e.preventDefault();
    if (!citaParaReprogramar) return;
    if (!reprogFecha || !reprogHora) {
      setReprogError('Debes seleccionar una nueva fecha y un horario disponible.');
      return;
    }
    setGuardandoReprog(true);
    setReprogError('');
    try {
      const nuevaFechaHora = new Date(reprogHora).toISOString();
      await api(`/api/citas/${citaParaReprogramar.id}/reprogramar`, {
        method: 'PATCH',
        body: { fechaHora: nuevaFechaHora },
      });
      setSuccess(`Cita #${citaParaReprogramar.id} reprogramada con éxito.`);
      setShowReprogramarModal(false);
      setCitaParaReprogramar(null);
      cargarDatos();
    } catch (err) {
      setReprogError(err.message || 'Error al reprogramar la cita. Verifica que el horario esté libre.');
    } finally {
      setGuardandoReprog(false);
    }
  };

  const hoyStr = new Date().toISOString().split('T')[0];

  const citasFiltradas = citas.filter((c) => {
    if (filtroTexto.trim()) {
      const q = filtroTexto.toLowerCase().trim();
      const pacDni = pacientes.find((p) => p.id === c.pacienteId)?.dni || '';
      const matchPac = (c.pacienteNombre || '').toLowerCase().includes(q);
      const matchMed = (c.medicoNombre || '').toLowerCase().includes(q);
      const matchDni = pacDni.toLowerCase().includes(q);
      const matchId = String(c.id).includes(q);
      if (!matchPac && !matchMed && !matchDni && !matchId) return false;
    }

    if (filtroEstado !== 'TODAS' && c.estado !== filtroEstado) {
      return false;
    }

    if (filtroFecha === 'HOY') {
      const fCita = (c.fechaHora || '').split('T')[0];
      if (fCita !== hoyStr) return false;
    } else if (filtroFecha === 'FUTURAS') {
      const fCita = (c.fechaHora || '').split('T')[0];
      if (fCita < hoyStr) return false;
    }

    return true;
  });

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado Principal */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            {isPaciente ? 'Reservar Cita Médica' : 'Gestión y Programación de Citas'}
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            {isPaciente
              ? 'Elige tu especialidad médica de interés, médico tratante y horario disponible.'
              : 'Administra la agenda médica hospitalaria y programa citas para los pacientes.'}
          </p>
        </div>

        {!isPaciente && (
          <button
            type="button"
            onClick={() => setShowNuevoPaciente(!showNuevoPaciente)}
            aria-expanded={showNuevoPaciente}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-sky-50 border border-sky-300 text-[#006591] hover:bg-sky-100 font-bold text-xs rounded-xl shadow-2xs transition-all self-start min-h-[44px]"
          >
            <span className="material-symbols-outlined text-[18px]">person_add</span>
            {showNuevoPaciente ? 'Ocultar Formulario de Registro' : '+ Registrar Nuevo Paciente'}
          </button>
        )}
      </div>

      {/* Alertas Accesibles */}
      {error && (
        <div role="alert" aria-live="polite" className="p-4 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl flex items-start gap-3 shadow-xs">
          <span className="material-symbols-outlined text-rose-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            error
          </span>
          <div className="text-xs text-rose-800 font-medium">{error}</div>
        </div>
      )}

      {success && (
        <div role="alert" aria-live="polite" className="p-4 bg-emerald-50 border-l-4 border-emerald-600 rounded-r-xl flex items-start gap-3 shadow-xs">
          <span className="material-symbols-outlined text-emerald-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            check_circle
          </span>
          <div className="text-xs text-emerald-800 font-medium">{success}</div>
        </div>
      )}

      {/* Formulario colapsable para registro rápido de paciente */}
      {showNuevoPaciente && (
        <section
          aria-labelledby="registro-paciente-title"
          className="bg-slate-50/90 border border-slate-300 rounded-2xl p-6 space-y-4 shadow-xs"
        >
          <div className="flex items-center gap-2 text-[#006591] pb-2 border-b border-slate-200">
            <span className="material-symbols-outlined text-[24px]">badge</span>
            <h2 id="registro-paciente-title" className="text-sm font-bold tracking-tight">
              Registrar Nuevo Paciente en el Sistema
            </h2>
          </div>

          <form onSubmit={handleCrearPaciente} className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label htmlFor="pac-dni" className="text-xs font-bold text-slate-700 block mb-1">
                DNI (8 dígitos) <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-dni"
                type="text"
                required
                maxLength={8}
                pattern="\d{8}"
                placeholder="12345678"
                value={pacienteForm.dni}
                onChange={(e) => setPacienteForm({ ...pacienteForm, dni: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 font-mono focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div>
              <label htmlFor="pac-nombres" className="text-xs font-bold text-slate-700 block mb-1">
                Nombres <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-nombres"
                type="text"
                required
                placeholder="Nombres completos"
                value={pacienteForm.nombres}
                onChange={(e) => setPacienteForm({ ...pacienteForm, nombres: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div>
              <label htmlFor="pac-apellidos" className="text-xs font-bold text-slate-700 block mb-1">
                Apellidos <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-apellidos"
                type="text"
                required
                placeholder="Apellidos paterno y materno"
                value={pacienteForm.apellidos}
                onChange={(e) => setPacienteForm({ ...pacienteForm, apellidos: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div>
              <label htmlFor="pac-fnac" className="text-xs font-bold text-slate-700 block mb-1">
                Fecha de Nacimiento <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-fnac"
                type="date"
                required
                value={pacienteForm.fechaNacimiento}
                onChange={(e) => setPacienteForm({ ...pacienteForm, fechaNacimiento: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div>
              <label htmlFor="pac-tel" className="text-xs font-bold text-slate-700 block mb-1">
                Teléfono de Contacto (9 dígitos) <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-tel"
                type="tel"
                required
                pattern="\d{9}"
                placeholder="987654321"
                value={pacienteForm.telefono}
                onChange={(e) => setPacienteForm({ ...pacienteForm, telefono: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div>
              <label htmlFor="pac-correo" className="text-xs font-bold text-slate-700 block mb-1">
                Correo Electrónico <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <input
                id="pac-correo"
                type="email"
                required
                placeholder="paciente@correo.com"
                value={pacienteForm.correo}
                onChange={(e) => setPacienteForm({ ...pacienteForm, correo: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            </div>

            <div className="md:col-span-3 flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setShowNuevoPaciente(false)}
                className="px-4 py-2 border border-slate-300 text-slate-700 rounded-xl text-xs font-semibold hover:bg-slate-100 min-h-[40px]"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="bg-[#006591] text-white px-5 py-2 rounded-xl text-xs font-bold hover:bg-[#004d70] shadow-sm min-h-[40px]"
              >
                Guardar y Registrar Paciente
              </button>
            </div>
          </form>
        </section>
      )}

      {/* Formulario de Agendamiento de Citas */}
      <section aria-labelledby="form-agendar-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6">
        <div className="pb-4 border-b border-slate-200">
          <h2 id="form-agendar-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <span className="material-symbols-outlined text-[#006591]">calendar_add_on</span>
            {isPaciente ? 'Reservar Cita Médica' : 'Agendar Nueva Cita'}
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">Completa los campos a continuación para registrar la consulta en la agenda hospitalaria.</p>
        </div>

        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {!isPaciente && (
            <div>
              <label htmlFor="select-paciente-cita" className="text-xs font-bold text-slate-700 block mb-1">
                Paciente <span className="text-rose-600" aria-hidden="true">*</span>
              </label>
              <select
                id="select-paciente-cita"
                required
                value={form.pacienteId}
                onChange={(e) => setForm({ ...form, pacienteId: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              >
                <option value="">Selecciona paciente registrado...</option>
                {pacientes.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombres} {p.apellidos} (DNI: {p.dni})
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label htmlFor="select-filtro-esp" className="text-xs font-bold text-slate-700 block mb-1">
              Filtrar Especialidad
            </label>
            <select
              id="select-filtro-esp"
              value={especialidadFiltro}
              onChange={(e) => setEspecialidadFiltro(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            >
              <option value="">Todas las especialidades</option>
              {especialidades.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="select-medico-cita" className="text-xs font-bold text-slate-700 block mb-1">
              Médico Tratante <span className="text-rose-600" aria-hidden="true">*</span>
            </label>
            <select
              id="select-medico-cita"
              required
              value={form.medicoId}
              onChange={(e) => handleMedicoChange(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            >
              <option value="">Selecciona médico...</option>
              {medicosFiltrados.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nombreCompleto || `${m.nombres} ${m.apellidos}`} ({m.especialidad?.nombre || 'General'})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="fecha-cita-input" className="text-xs font-bold text-slate-700 block mb-1">
              Fecha de Atención <span className="text-rose-600" aria-hidden="true">*</span>
            </label>
            <input
              id="fecha-cita-input"
              type="date"
              required
              value={form.fecha}
              onChange={(e) => handleFechaChange(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            />
          </div>

          <div>
            <label htmlFor="hora-cita-select" className="text-xs font-bold text-slate-700 block mb-1">
              Horario Disponible {loadingSlots && '(consultando disponibilidad...)'}{' '}
              <span className="text-rose-600" aria-hidden="true">*</span>
            </label>
            {slotsDisponibles.length > 0 ? (
              <select
                id="hora-cita-select"
                required
                value={form.hora}
                onChange={(e) => setForm({ ...form, hora: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              >
                <option value="">Selecciona horario disponible...</option>
                {slotsDisponibles.map((s, idx) => (
                  <option key={idx} value={s}>
                    {new Date(s).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })}
                  </option>
                ))}
              </select>
            ) : (
              <input
                id="hora-cita-select"
                type="time"
                required
                value={form.hora ? form.hora.substring(11, 16) : ''}
                onChange={(e) => setForm({ ...form, hora: `${form.fecha}T${e.target.value}:00Z` })}
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
              />
            )}
          </div>

          <div>
            <label htmlFor="prioridad-cita-select" className="text-xs font-bold text-slate-700 block mb-1">
              Nivel de Prioridad <span className="text-rose-600" aria-hidden="true">*</span>
            </label>
            <select
              id="prioridad-cita-select"
              value={form.prioridad}
              onChange={(e) => setForm({ ...form, prioridad: e.target.value })}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            >
              <option value="NORMAL">Normal (Consulta de rutina)</option>
              <option value="URGENTE">Urgente (Atención prioritaria en triaje)</option>
            </select>
          </div>

          <div className="md:col-span-2 lg:col-span-3">
            <label htmlFor="motivo-cita-input" className="text-xs font-bold text-slate-700 block mb-1">
              Motivo de la Cita o Síntomas <span className="text-rose-600" aria-hidden="true">*</span>
            </label>
            <input
              id="motivo-cita-input"
              type="text"
              required
              placeholder="Describe brevemente tus síntomas principales o el motivo de la consulta..."
              value={form.motivoConsulta}
              onChange={(e) => setForm({ ...form, motivoConsulta: e.target.value })}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            />
          </div>

          <div className="md:col-span-2 lg:col-span-3 flex justify-end pt-2">
            <button
              type="submit"
              disabled={guardandoCita}
              className="bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-7 py-3 rounded-xl text-sm font-bold transition-all shadow-sm flex items-center gap-2 min-h-[44px]"
            >
              <span className="material-symbols-outlined text-[20px]">calendar_today</span>
              {guardandoCita ? 'Confirmando...' : 'Confirmar y Agendar Cita'}
            </button>
          </div>
        </form>
      </section>

      {/* Listado Accesible de Citas Registradas con Filtros de Ventanilla */}
      <section aria-labelledby="listado-citas-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-200 space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <h2 id="listado-citas-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006591]">calendar_month</span>
                {isPaciente ? 'Mis Citas Agendadas' : 'Control de Citas y Atención en Ventanilla'}
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                {isPaciente
                  ? 'Gestiona tus citas médicas programadas, solicita reprogramación de fecha o cancela si no podrás asistir.'
                  : 'Recepción, confirmación de llegada (Check-in en sala), reprogramación y seguimiento de agenda en ventanilla.'}
              </p>
            </div>
            <div className="flex items-center gap-2 self-start">
              <span className="text-xs font-bold text-slate-700 px-3 py-1 bg-slate-100 rounded-full">
                {citasFiltradas.length} de {citas.length} citas
              </span>
            </div>
          </div>

          {/* Barra de Filtros Rápidos de Ventanilla */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 pt-2">
            {/* Buscador por DNI o Nombre */}
            <div className="relative sm:col-span-2">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-[18px]" aria-hidden="true">
                search
              </span>
              <input
                type="text"
                value={filtroTexto}
                onChange={(e) => setFiltroTexto(e.target.value)}
                placeholder={isPaciente ? "Buscar por médico o N° de cita..." : "Buscar por paciente, DNI, médico o N° cita..."}
                className="w-full pl-10 pr-9 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[38px]"
              />
              {filtroTexto && (
                <button
                  type="button"
                  onClick={() => setFiltroTexto('')}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5 rounded cursor-pointer"
                  aria-label="Limpiar búsqueda"
                >
                  <span className="material-symbols-outlined text-[16px]">cancel</span>
                </button>
              )}
            </div>

            {/* Filtro por Estado */}
            <div>
              <select
                aria-label="Filtrar por estado de cita"
                value={filtroEstado}
                onChange={(e) => setFiltroEstado(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-800 font-medium focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[38px]"
              >
                <option value="TODAS">Todos los Estados</option>
                <option value="PENDIENTE">⏳ Pendientes</option>
                <option value="CONFIRMADA">✅ Confirmadas (Check-in)</option>
                <option value="REPROGRAMADA">🔄 Reprogramadas</option>
                <option value="ATENDIDA">🩺 Atendidas</option>
                <option value="CANCELADA">❌ Canceladas</option>
              </select>
            </div>

            {/* Filtro por Fecha */}
            <div>
              <select
                aria-label="Filtrar por fecha de atención"
                value={filtroFecha}
                onChange={(e) => setFiltroFecha(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-800 font-medium focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[38px]"
              >
                <option value="TODAS">Todas las Fechas</option>
                <option value="HOY">📅 Citas de Hoy</option>
                <option value="FUTURAS">📆 Citas Próximas</option>
              </select>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <caption className="sr-only">Listado de citas programadas del paciente o del centro clínico</caption>
            <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
              <tr>
                <th scope="col" className="py-3 px-4">Paciente</th>
                <th scope="col" className="py-3 px-4">Médico</th>
                <th scope="col" className="py-3 px-4">Fecha y Hora</th>
                <th scope="col" className="py-3 px-4">Prioridad</th>
                <th scope="col" className="py-3 px-4">Estado</th>
                <th scope="col" className="py-3 px-4">Motivo</th>
                <th scope="col" className="py-3 px-4 text-right">Acciones de Ventanilla</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-xs text-slate-500">
                    <div className="flex items-center justify-center gap-2">
                      <span className="material-symbols-outlined animate-spin text-[#006591]">progress_activity</span>
                      Cargando citas...
                    </div>
                  </td>
                </tr>
              ) : citasFiltradas.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-10 text-center text-xs text-slate-500">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <span className="material-symbols-outlined text-3xl text-slate-400">event_busy</span>
                      <p className="font-semibold text-slate-700">
                        {citas.length === 0
                          ? (isPaciente ? 'No tienes citas agendadas actualmente.' : 'No hay citas registradas en la base de datos.')
                          : 'No se encontraron citas con los filtros seleccionados.'}
                      </p>
                      {(filtroTexto || filtroEstado !== 'TODAS' || filtroFecha !== 'TODAS') && (
                        <button
                          type="button"
                          onClick={() => {
                            setFiltroTexto('');
                            setFiltroEstado('TODAS');
                            setFiltroFecha('TODAS');
                          }}
                          className="text-xs font-bold text-[#006591] hover:underline cursor-pointer mt-1"
                        >
                          Restablecer todos los filtros
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                citasFiltradas.map((c) => {
                  const pacDni = pacientes.find((p) => p.id === c.pacienteId)?.dni;
                  const fechaObj = new Date(c.fechaHora);
                  const esPasada = fechaObj < new Date();
                  const puedeModificar = c.estado !== 'COMPLETADA' && c.estado !== 'ATENDIDA' && c.estado !== 'CANCELADA';

                  return (
                    <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3.5 px-4 font-bold text-slate-900">
                        <span className="block leading-tight">{c.pacienteNombre}</span>
                        {pacDni && (
                          <span className="text-[11px] text-slate-500 font-mono font-normal">DNI: {pacDni}</span>
                        )}
                        <span className="text-[10px] text-slate-400 font-mono block">Cita #{c.id}</span>
                      </td>
                      <td className="py-3.5 px-4 text-xs font-medium text-slate-700">{c.medicoNombre}</td>
                      <td className="py-3.5 px-4 text-xs font-semibold text-slate-800">
                        <div className="flex items-center gap-1.5">
                          <span className="material-symbols-outlined text-[16px] text-slate-400">schedule</span>
                          <span>
                            {fechaObj.toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' })}
                          </span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                            c.prioridad === 'URGENTE' ? 'bg-rose-100 text-rose-800' : 'bg-sky-100 text-[#006591]'
                          }`}
                        >
                          {c.prioridad}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        {c.estado === 'COMPLETADA' || c.estado === 'ATENDIDA' ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
                            <span className="material-symbols-outlined text-[13px]">check_circle</span>
                            Atendida
                          </span>
                        ) : c.estado === 'CONFIRMADA' ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-sky-100 text-[#006591] border border-sky-200">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                            Confirmada (En sala)
                          </span>
                        ) : c.estado === 'REPROGRAMADA' ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-purple-100 text-purple-800 border border-purple-200">
                            <span className="material-symbols-outlined text-[13px]">update</span>
                            Reprogramada
                          </span>
                        ) : c.estado === 'CANCELADA' ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-100 text-rose-700 border border-rose-200">
                            <span className="material-symbols-outlined text-[13px]">cancel</span>
                            Cancelada
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
                            <span className="material-symbols-outlined text-[13px]">hourglass_empty</span>
                            Pendiente
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-xs text-slate-600 max-w-[200px] truncate" title={c.motivoConsulta}>
                        {c.motivoConsulta}
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5 flex-wrap">
                          {/* Botón de Check-in (Solo Recepcionista o Admin en citas pendientes o reprogramadas) */}
                          {!isPaciente && (c.estado === 'PENDIENTE' || c.estado === 'REPROGRAMADA') && (
                            <button
                              type="button"
                              onClick={() => handleConfirmarLlegada(c.id, c.pacienteNombre)}
                              title="Marcar llegada del paciente en ventanilla e ingresarlo a sala de espera"
                              className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-bold text-emerald-800 bg-emerald-50 hover:bg-emerald-100 border border-emerald-300 rounded-lg transition-colors cursor-pointer min-h-[32px]"
                            >
                              <span className="material-symbols-outlined text-[15px] text-emerald-600">how_to_reg</span>
                              <span>Check-in</span>
                            </button>
                          )}

                          {/* Botón de Reprogramar (Paciente, Recepcionista, Admin) */}
                          {puedeModificar && (
                            <button
                              type="button"
                              onClick={() => abrirModalReprogramar(c)}
                              title="Reprogramar fecha u horario de la cita"
                              className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-bold text-[#006591] bg-sky-50 hover:bg-sky-100 border border-sky-300 rounded-lg transition-colors cursor-pointer min-h-[32px]"
                            >
                              <span className="material-symbols-outlined text-[15px]">edit_calendar</span>
                              <span>Reprogramar</span>
                            </button>
                          )}

                          {/* Botón de Cancelar Cita */}
                          {puedeModificar && (
                            <button
                              type="button"
                              onClick={() => {
                                if (window.confirm(`¿Estás seguro de cancelar la cita de ${c.pacienteNombre}?`)) {
                                  cambiarEstado(c.id, 'CANCELADA');
                                }
                              }}
                              title="Cancelar cita médica"
                              className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-bold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-300 rounded-lg transition-colors cursor-pointer min-h-[32px]"
                            >
                              <span className="material-symbols-outlined text-[15px]">event_busy</span>
                              <span>Cancelar</span>
                            </button>
                          )}

                          {/* Etiqueta informativa cuando ya no es modificable */}
                          {(c.estado === 'COMPLETADA' || c.estado === 'ATENDIDA') && (
                            <span className="text-[11px] text-emerald-700 font-semibold flex items-center gap-1">
                              <span className="material-symbols-outlined text-[15px]">task_alt</span>
                              Atención finalizada
                            </span>
                          )}

                          {c.estado === 'CANCELADA' && (
                            <span className="text-[11px] text-slate-400 italic">
                              Sin acciones
                            </span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* Modal Accesible de Reprogramación de Cita */}
      {showReprogramarModal && citaParaReprogramar && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-reprogramar-title"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-150"
        >
          <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-lg p-6 space-y-5">
            {/* Header del Modal */}
            <div className="flex items-start justify-between pb-3 border-b border-slate-200">
              <div>
                <h3 id="modal-reprogramar-title" className="text-base font-bold text-slate-900 flex items-center gap-2">
                  <span className="material-symbols-outlined text-[#006591]">edit_calendar</span>
                  Reprogramar Cita Médica
                </h3>
                <p className="text-xs text-slate-600 mt-1">
                  Paciente: <span className="font-bold text-slate-800">{citaParaReprogramar.pacienteNombre}</span>
                </p>
                <p className="text-xs text-slate-500">
                  Médico: <span className="font-medium text-slate-700">{citaParaReprogramar.medicoNombre}</span>
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setShowReprogramarModal(false);
                  setCitaParaReprogramar(null);
                  setReprogError('');
                }}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg transition-colors cursor-pointer"
                aria-label="Cerrar modal de reprogramación"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            {/* Ficha del Horario Actual */}
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs flex items-center justify-between">
              <span className="text-slate-500 font-medium">Horario actual registrado:</span>
              <span className="font-bold text-slate-800 flex items-center gap-1">
                <span className="material-symbols-outlined text-[15px] text-slate-400">schedule</span>
                {new Date(citaParaReprogramar.fechaHora).toLocaleString('es-PE', { dateStyle: 'long', timeStyle: 'short' })}
              </span>
            </div>

            {reprogError && (
              <div role="alert" className="p-3 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl text-xs text-rose-800 font-medium">
                {reprogError}
              </div>
            )}

            <form noValidate onSubmit={handleConfirmarReprogramacion} className="space-y-4">
              {/* Selección de Nueva Fecha */}
              <div>
                <label htmlFor="input-nueva-fecha" className="text-xs font-bold text-slate-700 block mb-1">
                  Selecciona la Nueva Fecha <span className="text-rose-600">*</span>
                </label>
                <input
                  id="input-nueva-fecha"
                  type="date"
                  required
                  min={hoyStr}
                  value={reprogFecha}
                  onChange={(e) => handleReprogFechaChange(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
                <span className="text-[11px] text-slate-500 mt-1 block">
                  Al elegir fecha, se consultarán en tiempo real los turnos libres del médico.
                </span>
              </div>

              {/* Selección de Nuevo Horario Disponible */}
              {reprogFecha && (
                <div>
                  <label className="text-xs font-bold text-slate-700 block mb-1.5">
                    Horarios Disponibles para esta Fecha <span className="text-rose-600">*</span>
                  </label>

                  {loadingSlotsReprog ? (
                    <div className="p-4 text-center text-xs text-slate-500 flex items-center justify-center gap-2">
                      <span className="material-symbols-outlined animate-spin text-[#006591] text-[18px]">progress_activity</span>
                      Consultando disponibilidad del médico...
                    </div>
                  ) : slotsReprog.length === 0 ? (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-900">
                      El médico no cuenta con turnos disponibles en la fecha seleccionada. Por favor selecciona otra fecha.
                    </div>
                  ) : (
                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-48 overflow-y-auto p-1">
                      {slotsReprog.map((slot) => {
                        const dateSlot = new Date(slot);
                        const horaTexto = dateSlot.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
                        const esSeleccionado = reprogHora === slot;

                        return (
                          <button
                            key={slot}
                            type="button"
                            onClick={() => {
                              setReprogHora(slot);
                              setReprogError('');
                            }}
                            className={`px-3 py-2 text-xs font-bold rounded-xl transition-all border cursor-pointer text-center ${
                              esSeleccionado
                                ? 'bg-[#006591] text-white border-[#006591] shadow-xs'
                                : 'bg-white hover:bg-sky-50 text-slate-700 border-slate-200'
                            }`}
                          >
                            {horaTexto}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {/* Botones de Acción */}
              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => {
                    setShowReprogramarModal(false);
                    setCitaParaReprogramar(null);
                    setReprogError('');
                  }}
                  className="px-4 py-2.5 text-xs font-bold text-slate-600 hover:text-slate-800 bg-slate-100 hover:bg-slate-200 rounded-xl transition-all cursor-pointer min-h-[40px]"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={!reprogHora || guardandoReprog}
                  className="px-5 py-2.5 text-xs font-bold text-white bg-[#006591] hover:bg-[#004d70] disabled:opacity-50 rounded-xl shadow-xs transition-all flex items-center gap-1.5 min-h-[40px] cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[16px]">check</span>
                  {guardandoReprog ? 'Reprogramando...' : 'Confirmar Nueva Fecha'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}