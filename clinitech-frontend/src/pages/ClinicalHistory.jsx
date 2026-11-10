import { useState, useEffect } from 'react';
import { api } from '../api';

export default function ClinicalHistory() {
  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  const role = user.role || '';
  const isPaciente = role === 'PACIENTE';
  const isMedico = role === 'MEDICO';
  const isRecepcionista = role === 'RECEPCIONISTA';
  const isAdmin = role === 'ADMIN';
  const puedeAdmitir = isAdmin || isRecepcionista;

  const [tab, setTab] = useState('buscar'); // 'buscar' | 'directorio'
  const [dni, setDni] = useState('');
  const [paciente, setPaciente] = useState(null);
  const [consultas, setConsultas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Formulario de Admisión de Paciente Nuevo
  const [showAdmision, setShowAdmision] = useState(false);
  const [guardandoAdmision, setGuardandoAdmision] = useState(false);
  const [admisionForm, setAdmisionForm] = useState({
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

  // Lista de pacientes del directorio general
  const [pacientesDirectorio, setPacientesDirectorio] = useState([]);
  const [loadingDirectorio, setLoadingDirectorio] = useState(false);

  const buscarPaciente = async (e) => {
    e?.preventDefault();
    const dniLimpio = dni.trim();
    if (!dniLimpio) return;
    setLoading(true);
    setError('');
    setSuccess('');
    setShowAdmision(false);
    try {
      const pac = await api(`/api/pacientes/arbol/buscar/${dniLimpio}`);
      setPaciente(pac);

      // Cargar historial de atenciones clínicas de forma aislada
      try {
        const hist = await api(`/api/consultas?pacienteId=${pac.id}`);
        setConsultas(Array.isArray(hist) ? hist : (hist?.content || []));
      } catch (histErr) {
        console.warn('No se pudieron cargar las consultas del paciente:', histErr);
        setConsultas([]);
      }
    } catch {
      setPaciente(null);
      setConsultas([]);
      if (puedeAdmitir) {
        setError(`El documento DNI ${dniLimpio} no fue encontrado en el sistema. Puedes completar el formulario de admisión rápida a continuación.`);
        setShowAdmision(true);
        setAdmisionForm((prev) => ({ ...prev, dni: dniLimpio }));
      } else {
        setError(`El documento DNI ${dniLimpio} no fue encontrado en el sistema.`);
        setShowAdmision(false);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAdmitirPaciente = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setGuardandoAdmision(true);
    try {
      const nuevo = await api('/api/pacientes', {
        method: 'POST',
        body: admisionForm,
      });
      setSuccess(`Paciente ${nuevo.nombres} ${nuevo.apellidos} registrado y admitido con éxito.`);
      setShowAdmision(false);
      setPaciente(nuevo);
      setConsultas([]);
      setDni(nuevo.dni);
      cargarDirectorio();
    } catch (err) {
      setError(err.message || 'Error al admitir paciente.');
    } finally {
      setGuardandoAdmision(false);
    }
  };

  const cargarDirectorio = async () => {
    setLoadingDirectorio(true);
    try {
      const lista = await api('/api/pacientes/arbol/en-orden');
      setPacientesDirectorio(Array.isArray(lista) ? lista : []);
    } catch {
      setPacientesDirectorio([]);
    } finally {
      setLoadingDirectorio(false);
    }
  };

  useEffect(() => {
    if (tab === 'directorio' && !isPaciente) {
      cargarDirectorio();
    }
  }, [tab, isPaciente]);

  useEffect(() => {
    if (isPaciente && user.pacienteId) {
      const cargarHistorialPropio = async () => {
        setLoading(true);
        setError('');
        try {
          const pac = await api(`/api/pacientes/${user.pacienteId}`);
          setPaciente(pac);
          setDni(pac.dni);
          const hist = await api(`/api/consultas?pacienteId=${user.pacienteId}`);
          setConsultas(Array.isArray(hist) ? hist : (hist?.content || []));
        } catch (err) {
          setError(err.message || 'Error al cargar tu historial clínico.');
        } finally {
          setLoading(false);
        }
      };
      cargarHistorialPropio();
    }
  }, [isPaciente, user.pacienteId]);

  const seleccionarPacienteDeDirectorio = async (p) => {
    setPaciente(p);
    setDni(p.dni);
    setTab('buscar');
    setShowAdmision(false);
    try {
      const hist = await api(`/api/consultas?pacienteId=${p.id}`);
      setConsultas(Array.isArray(hist) ? hist : (hist?.content || []));
    } catch {
      setConsultas([]);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado y Selector Accesible de Pestañas */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            {isPaciente ? 'Mi Historial Clínico' : (isMedico ? 'Ficha Clínica y Diagnóstico' : 'Admisión y Ficha Clínica')}
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            {isPaciente
              ? 'Consulta tus datos clínicos registrados, diagnósticos y atenciones médicas realizadas.'
              : isMedico
              ? 'Búsqueda de pacientes por DNI y consulta de antecedentes clínicos.'
              : 'Búsqueda instantánea de pacientes por DNI, registro de admisión y consulta de historial médico.'}
          </p>
        </div>

        {/* Tablist Accesible (solo para personal administrativo y médico) */}
        {!isPaciente && (
          <div
            role="tablist"
            aria-label="Modo de consulta de pacientes"
            className="flex bg-slate-200/80 p-1 rounded-2xl self-start"
          >
            <button
              role="tab"
              id="tab-buscar"
              aria-selected={tab === 'buscar'}
              aria-controls="panel-buscar"
              onClick={() => setTab('buscar')}
              className={`px-5 py-2 rounded-xl text-xs font-bold transition-all min-h-[40px] flex items-center gap-1.5 ${
                tab === 'buscar'
                  ? 'bg-white text-[#006591] shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <span className="material-symbols-outlined text-[18px]">search</span>
              Búsqueda por DNI
            </button>
            <button
              role="tab"
              id="tab-directorio"
              aria-selected={tab === 'directorio'}
              aria-controls="panel-directorio"
              onClick={() => setTab('directorio')}
              className={`px-5 py-2 rounded-xl text-xs font-bold transition-all min-h-[40px] flex items-center gap-1.5 ${
                tab === 'directorio'
                  ? 'bg-white text-[#006591] shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <span className="material-symbols-outlined text-[18px]">groups</span>
              Directorio de Pacientes
            </button>
          </div>
        )}
      </div>

      {/* Alertas Accesibles */}
      {error && (
        <div role="alert" aria-live="polite" className="p-4 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl flex items-start gap-3 shadow-xs">
          <span className="material-symbols-outlined text-rose-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            info
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

      {/* PESTAÑA 1: BUSCADOR POR DNI Y ADMISIÓN */}
      {tab === 'buscar' && (
        <div id="panel-buscar" role="tabpanel" aria-labelledby="tab-buscar" className="space-y-6">
          {/* Cargando historial propio de paciente */}
          {isPaciente && loading && (
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-12 text-center">
              <span className="material-symbols-outlined text-[#006591] text-[40px] animate-spin">
                progress_activity
              </span>
              <p className="text-sm font-semibold text-slate-600 mt-2">Cargando tu información clínica...</p>
            </div>
          )}

          {isPaciente && !loading && !paciente && (
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-12 text-center">
              <span className="material-symbols-outlined text-slate-400 text-[48px]">
                medical_information
              </span>
              <p className="text-sm font-semibold text-slate-700 mt-2">No se encontró una ficha clínica vinculada a tu cuenta.</p>
              <p className="text-xs text-slate-500 mt-1">Por favor, acércate a recepción o solicita la verificación de tus datos.</p>
            </div>
          )}

          {/* Buscador Rápido por DNI (Solo personal médico/recepción) */}
          {!isPaciente && (
            <form
              onSubmit={buscarPaciente}
              className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 flex flex-col sm:flex-row gap-4 items-end"
            >
              <div className="flex-1 w-full">
                <label htmlFor="input-dni-buscar" className="text-xs font-bold text-slate-700 block mb-1.5">
                  Número de Documento (DNI del Paciente) <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <div className="relative">
                  <span className="absolute left-3.5 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-[20px]" aria-hidden="true">
                    badge
                  </span>
                  <input
                    id="input-dni-buscar"
                    type="text"
                    required
                    maxLength={8}
                    pattern="\d{8}"
                    placeholder="Ingresa los 8 dígitos del DNI..."
                    value={dni}
                    onChange={(e) => setDni(e.target.value)}
                    className="w-full pl-11 pr-4 py-3 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 font-mono tracking-wider focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full sm:w-auto bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-7 py-3 rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2 shadow-sm min-h-[44px]"
              >
                <span className="material-symbols-outlined text-[20px]">search</span>
                {loading ? 'Buscando paciente...' : 'Buscar Paciente'}
              </button>
            </form>
          )}

          {/* Formulario de Admisión de Paciente si no existe en el sistema (Exclusivo Admisión / Recepción) */}
          {puedeAdmitir && showAdmision && (
            <section
              aria-labelledby="form-admision-title"
              className="bg-sky-50/60 border-2 border-dashed border-[#006591] rounded-2xl p-6 space-y-4 shadow-xs animate-in fade-in duration-150"
            >
              <div className="flex items-center gap-2.5 text-[#006591] pb-2 border-b border-sky-200">
                <span className="material-symbols-outlined text-[24px]">person_add</span>
                <h2 id="form-admision-title" className="text-base font-bold text-slate-900">
                  Admisión Rápida de Paciente Nuevo
                </h2>
              </div>
              <p className="text-xs text-slate-600">
                El paciente no registra historia clínica previa. Ingresa sus datos personales para darlo de alta en el centro de salud.
              </p>

              <form onSubmit={handleAdmitirPaciente} className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                <div>
                  <label htmlFor="adm-dni" className="text-xs font-bold text-slate-700 block mb-1">
                    DNI <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-dni"
                    type="text"
                    required
                    maxLength={8}
                    pattern="\d{8}"
                    value={admisionForm.dni}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, dni: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 font-mono focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="adm-nombres" className="text-xs font-bold text-slate-700 block mb-1">
                    Nombres <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-nombres"
                    type="text"
                    required
                    value={admisionForm.nombres}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, nombres: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="adm-apellidos" className="text-xs font-bold text-slate-700 block mb-1">
                    Apellidos <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-apellidos"
                    type="text"
                    required
                    value={admisionForm.apellidos}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, apellidos: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="adm-fnac" className="text-xs font-bold text-slate-700 block mb-1">
                    Fecha de Nacimiento <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-fnac"
                    type="date"
                    required
                    value={admisionForm.fechaNacimiento}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, fechaNacimiento: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="adm-tel" className="text-xs font-bold text-slate-700 block mb-1">
                    Teléfono (9 dígitos) <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-tel"
                    type="tel"
                    required
                    pattern="\d{9}"
                    value={admisionForm.telefono}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, telefono: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="adm-correo" className="text-xs font-bold text-slate-700 block mb-1">
                    Correo Electrónico <span className="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="adm-correo"
                    type="email"
                    required
                    value={admisionForm.correo}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, correo: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div className="sm:col-span-2 md:col-span-3">
                  <label htmlFor="adm-alergias" className="text-xs font-bold text-slate-700 block mb-1">
                    Alergias Conocidas
                  </label>
                  <input
                    id="adm-alergias"
                    type="text"
                    placeholder="Ej. Penicilina, sulfamidas, mariscos, o ninguna..."
                    value={admisionForm.alergias}
                    onChange={(e) => setAdmisionForm({ ...admisionForm, alergias: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  />
                </div>

                <div className="sm:col-span-2 md:col-span-3 flex justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowAdmision(false)}
                    className="px-4 py-2 border border-slate-300 text-slate-700 rounded-xl text-xs font-semibold hover:bg-slate-100 min-h-[40px]"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={guardandoAdmision}
                    className="bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-6 py-2 rounded-xl text-xs font-bold shadow-sm min-h-[40px]"
                  >
                    {guardandoAdmision ? 'Admitiendo...' : 'Registrar y Admitir Paciente'}
                  </button>
                </div>
              </form>
            </section>
          )}

          {/* Ficha Médica e Historial Clínico del Paciente */}
          {paciente && (
            <article
              aria-labelledby="ficha-paciente-title"
              className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 sm:p-8 space-y-6 animate-in fade-in duration-150"
            >
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-slate-200">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-[#006591] to-[#0284c7] text-white flex items-center justify-center font-extrabold text-xl shadow-xs shrink-0">
                    {paciente.nombres.substring(0, 1)}
                    {paciente.apellidos.substring(0, 1)}
                  </div>
                  <div>
                    <span className="text-[11px] font-extrabold text-[#006591] uppercase tracking-wider block">
                      Ficha Médica del Paciente
                    </span>
                    <h2 id="ficha-paciente-title" className="text-xl sm:text-2xl font-extrabold text-slate-900">
                      {paciente.nombres} {paciente.apellidos}
                    </h2>
                    <p className="text-xs text-slate-500 font-mono mt-0.5">DNI: {paciente.dni}</p>
                  </div>
                </div>

                <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-full text-xs font-bold self-start">
                  <span className="w-2 h-2 rounded-full bg-emerald-500" aria-hidden="true" />
                  Paciente Activo
                </span>
              </div>

              {/* Datos de contacto y médicos */}
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 text-xs bg-slate-50 p-4 rounded-xl border border-slate-200">
                <div>
                  <span className="text-slate-500 block font-semibold">Teléfono:</span>
                  <span className="font-bold text-slate-900 mt-0.5 block">{paciente.telefono || '-'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block font-semibold">Correo Electrónico:</span>
                  <span className="font-bold text-slate-900 mt-0.5 block truncate">{paciente.correo || '-'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block font-semibold">Alergias:</span>
                  <span className={`font-bold mt-0.5 block ${paciente.alergias ? 'text-rose-700' : 'text-slate-700'}`}>
                    {paciente.alergias || 'Ninguna registrada'}
                  </span>
                </div>
                <div>
                  <span className="text-slate-500 block font-semibold">Antecedentes Médicos:</span>
                  <span className="font-bold text-slate-900 mt-0.5 block">{paciente.historialMedico || 'Sin antecedentes'}</span>
                </div>
              </div>

              {/* Historial Clínico de Consultas */}
              <section aria-labelledby="historial-consultas-heading" className="pt-4 space-y-4">
                <div className="flex items-center justify-between">
                  <h3 id="historial-consultas-heading" className="text-base font-bold text-slate-900 flex items-center gap-2">
                    <span className="material-symbols-outlined text-[#006591]">history_edu</span>
                    Historial de Atenciones Clínicas ({consultas.length})
                  </h3>
                </div>

                {consultas.length === 0 ? (
                  <div className="p-8 text-center text-xs text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-300">
                    <span className="material-symbols-outlined text-3xl text-slate-400 mb-1">folder_open</span>
                    <p className="font-semibold text-slate-700">El paciente no registra atenciones médicas pasadas.</p>
                    <p className="text-slate-500 mt-0.5">Sus futuras consultas y recetas aparecerán registradas cronológicamente aquí.</p>
                  </div>
                ) : (
                  <div className="space-y-3.5">
                    {consultas.map((c) => (
                      <div
                        key={c.id}
                        className="p-5 bg-white rounded-xl border border-slate-200 shadow-2xs space-y-2 hover:border-slate-300 transition-colors"
                      >
                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1 pb-2 border-b border-slate-100">
                          <span className="font-bold text-[#006591] text-xs flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-[16px]">stethoscope</span>
                            Atendido por {c.medicoNombre}
                          </span>
                          <span className="text-[11px] font-medium text-slate-500">
                            {new Date(c.fechaAtencion).toLocaleString('es-PE', {
                              dateStyle: 'medium',
                              timeStyle: 'short',
                            })}
                          </span>
                        </div>
                        <p className="text-xs text-slate-800">
                          <strong className="text-slate-600 block sm:inline mr-1">Diagnóstico:</strong> {c.diagnostico}
                        </p>
                        <p className="text-xs text-slate-800">
                          <strong className="text-slate-600 block sm:inline mr-1">Tratamiento & Receta:</strong> {c.tratamiento}
                        </p>
                        {c.observaciones && (
                          <p className="text-xs text-slate-500 italic">
                            <strong className="text-slate-600 not-italic mr-1">Observaciones:</strong> {c.observaciones}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </section>
            </article>
          )}
        </div>
      )}

      {/* PESTAÑA 2: DIRECTORIO GENERAL DE PACIENTES */}
      {tab === 'directorio' && (
        <section
          id="panel-directorio"
          role="tabpanel"
          aria-labelledby="tab-directorio"
          className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden"
        >
          <div className="p-6 border-b border-slate-200 flex items-center justify-between">
            <div>
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006591]">groups</span>
                Directorio General de Pacientes Registrados
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Padrón clínico de pacientes ordenados numéricamente por documento nacional de identidad.
              </p>
            </div>
            <span className="text-xs font-bold text-slate-600 px-3 py-1 bg-slate-100 rounded-full">
              {pacientesDirectorio.length} registrados
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <caption className="sr-only">Directorio general de pacientes admitidos</caption>
              <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
                <tr>
                  <th scope="col" className="py-3 px-4">DNI</th>
                  <th scope="col" className="py-3 px-4">Paciente</th>
                  <th scope="col" className="py-3 px-4">Teléfono</th>
                  <th scope="col" className="py-3 px-4">Correo Electrónico</th>
                  <th scope="col" className="py-3 px-4">Alergias</th>
                  <th scope="col" className="py-3 px-4 text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {loadingDirectorio ? (
                  <tr>
                    <td colSpan="6" className="py-8 text-center text-xs text-slate-500">
                      Cargando lista de pacientes...
                    </td>
                  </tr>
                ) : pacientesDirectorio.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="py-10 text-center text-xs text-slate-500">
                      No hay pacientes registrados en el sistema todavía.
                    </td>
                  </tr>
                ) : (
                  pacientesDirectorio.map((p) => (
                    <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3.5 px-4 font-mono font-bold text-xs text-[#006591]">{p.dni}</td>
                      <td className="py-3.5 px-4 font-bold text-slate-900">
                        {p.nombres} {p.apellidos}
                      </td>
                      <td className="py-3.5 px-4 text-xs text-slate-700 font-medium">{p.telefono || '-'}</td>
                      <td className="py-3.5 px-4 text-xs text-slate-600">{p.correo || '-'}</td>
                      <td className="py-3.5 px-4 text-xs font-medium">
                        {p.alergias ? (
                          <span className="text-rose-700 font-bold">{p.alergias}</span>
                        ) : (
                          <span className="text-slate-400">Ninguna</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={() => seleccionarPacienteDeDirectorio(p)}
                          aria-label={`Ver ficha médica de ${p.nombres} ${p.apellidos}`}
                          className="px-3.5 py-1.5 text-xs bg-sky-50 text-[#006591] hover:bg-[#006591] hover:text-white rounded-xl font-bold transition-all shadow-2xs min-h-[36px]"
                        >
                          Ver Ficha Clínica
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}