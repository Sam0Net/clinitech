import { useEffect, useState } from 'react';
import { api } from '../api';

export default function DoctorsManagement() {
  const [tab, setTab] = useState('medicos'); // 'medicos' | 'especialidades'

  // Estados Médicos
  const [medicos, setMedicos] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [guardandoMedico, setGuardandoMedico] = useState(false);

  const [form, setForm] = useState({
    nombres: '',
    apellidos: '',
    cmp: '',
    especialidadId: '',
    telefono: '',
    correo: '',
    horarioAtencion: '08:00 - 16:00',
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [crearCuenta, setCrearCuenta] = useState(false);
  const [cuentaUser, setCuentaUser] = useState('');
  const [cuentaPass, setCuentaPass] = useState('');

  // Cuentas de usuario médico vinculadas
  const [usuariosMedicos, setUsuariosMedicos] = useState([]);

  // Modal para asignar credenciales a un médico existente
  const [showAssignUserModal, setShowAssignUserModal] = useState(false);
  const [medicoParaUsuario, setMedicoParaUsuario] = useState(null);
  const [assignUser, setAssignUser] = useState('');
  const [assignPass, setAssignPass] = useState('');
  const [assignErrors, setAssignErrors] = useState({});
  const [asignando, setAsignando] = useState(false);

  // Estados Especialidades y Derivaciones
  const [nuevaEsp, setNuevaEsp] = useState({ nombre: '', descripcion: '' });
  const [guardandoEsp, setGuardandoEsp] = useState(false);
  const [origenId, setOrigenId] = useState('');
  const [destinoId, setDestinoId] = useState('');
  const [rutaInterconsulta, setRutaInterconsulta] = useState(null);
  const [espSeleccionada, setEspSeleccionada] = useState('');
  const [relacionadas, setRelacionadas] = useState(null);
  const [loadingGrafo, setLoadingGrafo] = useState(false);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      setError('');
      const [medsData, espData, usersData] = await Promise.all([
        api('/api/medicos'),
        api('/api/especialidades'),
        api('/api/auth/usuarios?rol=MEDICO').catch(() => []),
      ]);
      setMedicos(Array.isArray(medsData) ? medsData : (medsData?.content || []));
      setEspecialidades(Array.isArray(espData) ? espData : (espData?.content || []));
      setUsuariosMedicos(Array.isArray(usersData) ? usersData : []);
    } catch (err) {
      setError(err.message || 'Error al cargar catálogo de personal médico.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const validarFormMedico = () => {
    const errors = {};
    if (!form.nombres.trim() || form.nombres.trim().length < 2) {
      errors.nombres = 'Ingresa los nombres del médico (mínimo 2 caracteres).';
    }
    if (!form.apellidos.trim() || form.apellidos.trim().length < 2) {
      errors.apellidos = 'Ingresa los apellidos del médico (mínimo 2 caracteres).';
    }
    if (!form.cmp.trim() || !/^[A-Za-z0-9-]{5,10}$/.test(form.cmp.trim())) {
      errors.cmp = 'El CMP debe tener entre 5 y 10 caracteres (ej. CMP-54321).';
    }
    if (!form.especialidadId) {
      errors.especialidadId = 'Debes seleccionar una especialidad médica.';
    }
    if (!form.telefono.trim() || !/^\d{9}$/.test(form.telefono.trim())) {
      errors.telefono = 'El teléfono es obligatorio y debe contener exactamente 9 dígitos numéricos.';
    }
    if (!form.correo.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correo.trim())) {
      errors.correo = 'El correo electrónico es obligatorio y debe ser válido.';
    }
    if (!form.horarioAtencion.trim()) {
      errors.horarioAtencion = 'Ingresa el horario de atención habitual (ej. 08:00 - 16:00).';
    }
    if (crearCuenta) {
      if (!cuentaUser.trim() || cuentaUser.trim().length < 4) {
        errors.cuentaUser = 'El nombre de usuario debe tener al menos 4 caracteres.';
      }
      if (!cuentaPass || cuentaPass.length < 6) {
        errors.cuentaPass = 'La contraseña debe tener al menos 6 caracteres.';
      }
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmitMedico = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validarFormMedico()) {
      return;
    }

    setGuardandoMedico(true);
    try {
      const nuevoMedico = await api('/api/medicos', {
        method: 'POST',
        body: {
          ...form,
          nombres: form.nombres.trim(),
          apellidos: form.apellidos.trim(),
          cmp: form.cmp.trim(),
          telefono: form.telefono.trim(),
          correo: form.correo.trim().toLowerCase(),
          especialidadId: Number(form.especialidadId),
        },
      });

      let cuentaCreada = false;
      if (crearCuenta && cuentaUser.trim() && cuentaPass) {
        try {
          await api('/api/auth/usuarios', {
            method: 'POST',
            body: {
              username: cuentaUser.trim(),
              password: cuentaPass,
              rol: 'MEDICO',
              medicoId: nuevoMedico.id,
            },
          });
          cuentaCreada = true;
        } catch (authErr) {
          if (authErr.status === 409 || (authErr.message && authErr.message.toLowerCase().includes('ya existe'))) {
            setError(`El médico fue registrado, pero el usuario "@${cuentaUser.trim()}" ya existe en el sistema. Puedes asignarle otro usuario desde la tabla inferior.`);
          } else {
            setError(`El médico fue registrado, pero ocurrió un problema al crear sus credenciales: ${authErr.message}. Puedes asignárselas desde la tabla.`);
          }
          cargarDatos();
          setGuardandoMedico(false);
          return;
        }
      }

      setSuccess(
        cuentaCreada
          ? `Médico Dr(a). ${nuevoMedico.nombres} ${nuevoMedico.apellidos} y su cuenta de acceso (@${cuentaUser.trim()}) fueron registrados correctamente.`
          : `Médico Dr(a). ${nuevoMedico.nombres} ${nuevoMedico.apellidos} registrado exitosamente en el plantel médico.`
      );
      setFieldErrors({});
      setForm({
        nombres: '',
        apellidos: '',
        cmp: '',
        especialidadId: '',
        telefono: '',
        correo: '',
        horarioAtencion: '08:00 - 16:00',
      });
      setCrearCuenta(false);
      setCuentaUser('');
      setCuentaPass('');
      cargarDatos();
    } catch (err) {
      if (err.fields && Object.keys(err.fields).length > 0) {
        setFieldErrors(err.fields);
      } else if (err.status === 409 || (err.message && err.message.toLowerCase().includes('conflicto'))) {
        setFieldErrors((prev) => ({
          ...prev,
          cmp: 'El número de colegiatura (CMP) ya se encuentra registrado para otro médico.',
        }));
      } else {
        setError(err.message || 'Error al registrar el médico.');
      }
    } finally {
      setGuardandoMedico(false);
    }
  };

  const handleAsignarUsuario = async (e) => {
    e.preventDefault();
    if (!medicoParaUsuario) return;
    setAssignErrors({});
    setError('');
    setSuccess('');

    const errors = {};
    if (!assignUser.trim() || assignUser.trim().length < 4) {
      errors.assignUser = 'El nombre de usuario debe tener al menos 4 caracteres.';
    }
    if (!assignPass || assignPass.length < 6) {
      errors.assignPass = 'La contraseña debe tener al menos 6 caracteres.';
    }
    if (Object.keys(errors).length > 0) {
      setAssignErrors(errors);
      return;
    }

    setAsignando(true);
    try {
      await api('/api/auth/usuarios', {
        method: 'POST',
        body: {
          username: assignUser.trim(),
          password: assignPass,
          rol: 'MEDICO',
          medicoId: medicoParaUsuario.id,
        },
      });
      setSuccess(`Cuenta institucional (@${assignUser.trim()}) asignada con éxito al Dr(a). ${medicoParaUsuario.nombres} ${medicoParaUsuario.apellidos}.`);
      setShowAssignUserModal(false);
      setMedicoParaUsuario(null);
      setAssignUser('');
      setAssignPass('');
      cargarDatos();
    } catch (err) {
      if (err.message && err.message.toLowerCase().includes('ya existe')) {
        setAssignErrors({ assignUser: 'Este nombre de usuario ya está registrado en el sistema. Elige otro.' });
      } else {
        setAssignErrors({ general: err.message || 'No se pudo crear la cuenta de usuario médico.' });
      }
    } finally {
      setAsignando(false);
    }
  };

  const handleDesactivarMedico = async (id, nombre) => {
    if (!window.confirm(`¿Estás seguro de que deseas desactivar al médico ${nombre}?`)) return;
    try {
      await api(`/api/medicos/${id}`, { method: 'DELETE' });
      setSuccess(`Médico desactivado correctamente.`);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'Error al desactivar médico.');
    }
  };

  const handleSubmitEspecialidad = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setGuardandoEsp(true);
    try {
      const creada = await api('/api/especialidades', {
        method: 'POST',
        body: nuevaEsp,
      });
      setSuccess(`Especialidad "${creada.nombre}" registrada exitosamente en el catálogo médico.`);
      setNuevaEsp({ nombre: '', descripcion: '' });
      cargarDatos();
    } catch (err) {
      setError(err.message || 'Error al crear especialidad.');
    } finally {
      setGuardandoEsp(false);
    }
  };

  const handleConsultarRuta = async (e) => {
    e.preventDefault();
    if (!origenId || !destinoId) return;
    setError('');
    setLoadingGrafo(true);
    try {
      const ruta = await api(`/api/especialidades/interconsultas/ruta?origenId=${origenId}&destinoId=${destinoId}`);
      setRutaInterconsulta(ruta);
    } catch (err) {
      setError(err.message || 'No se pudo calcular la ruta de derivación.');
      setRutaInterconsulta(null);
    } finally {
      setLoadingGrafo(false);
    }
  };

  const handleConsultarRelacionadas = async (id) => {
    if (!id) return;
    setEspSeleccionada(id);
    try {
      const rels = await api(`/api/especialidades/${id}/relacionadas`);
      setRelacionadas(rels);
    } catch (err) {
      setError(err.message);
      setRelacionadas(null);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado y Pestañas Accesibles */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Gestión Médica y Especialidades
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            Administración del personal médico, catálogo de especialidades y cálculo de rutas de interconsulta.
          </p>
        </div>

        {/* Tablist Accesible (WCAG 2.2 AA) */}
        <div
          role="tablist"
          aria-label="Secciones de administración clínica"
          className="flex bg-slate-200/80 p-1 rounded-2xl self-start"
        >
          <button
            role="tab"
            id="tab-medicos"
            aria-selected={tab === 'medicos'}
            aria-controls="panel-medicos"
            onClick={() => setTab('medicos')}
            className={`px-5 py-2 rounded-xl text-xs font-bold transition-all min-h-[40px] flex items-center gap-1.5 ${
              tab === 'medicos'
                ? 'bg-white text-[#006591] shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <span className="material-symbols-outlined text-[18px]">medical_services</span>
            Médicos ({medicos.length})
          </button>
          <button
            role="tab"
            id="tab-especialidades"
            aria-selected={tab === 'especialidades'}
            aria-controls="panel-especialidades"
            onClick={() => setTab('especialidades')}
            className={`px-5 py-2 rounded-xl text-xs font-bold transition-all min-h-[40px] flex items-center gap-1.5 ${
              tab === 'especialidades'
                ? 'bg-white text-[#006591] shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <span className="material-symbols-outlined text-[18px]">hub</span>
            Especialidades & Derivaciones ({especialidades.length})
          </button>
        </div>
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

      {/* PESTAÑA 1: MÉDICOS */}
      {tab === 'medicos' && (
        <div id="panel-medicos" role="tabpanel" aria-labelledby="tab-medicos" className="space-y-6">
          {/* Formulario nuevo médico */}
          <section aria-labelledby="nuevo-medico-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6">
            <div className="pb-4 border-b border-slate-200">
              <h2 id="nuevo-medico-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006591]">person_add</span>
                Registrar Nuevo Profesional Médico
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">Ingresa los datos colegiados y asigna la especialidad correspondiente.</p>
            </div>

            <form noValidate onSubmit={handleSubmitMedico} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              <div>
                <label htmlFor="doc-nombres" className="text-xs font-bold text-slate-700 block mb-1">
                  Nombres <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="doc-nombres"
                  type="text"
                  placeholder="Ej. Roberto Carlos"
                  value={form.nombres}
                  aria-invalid={!!fieldErrors.nombres}
                  aria-describedby={fieldErrors.nombres ? 'err-doc-nombres' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, nombres: e.target.value });
                    if (fieldErrors.nombres) setFieldErrors({ ...fieldErrors, nombres: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.nombres
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.nombres && (
                  <p id="err-doc-nombres" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.nombres}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="doc-apellidos" className="text-xs font-bold text-slate-700 block mb-1">
                  Apellidos <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="doc-apellidos"
                  type="text"
                  placeholder="Ej. Gómez Silva"
                  value={form.apellidos}
                  aria-invalid={!!fieldErrors.apellidos}
                  aria-describedby={fieldErrors.apellidos ? 'err-doc-apellidos' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, apellidos: e.target.value });
                    if (fieldErrors.apellidos) setFieldErrors({ ...fieldErrors, apellidos: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.apellidos
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.apellidos && (
                  <p id="err-doc-apellidos" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.apellidos}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="doc-cmp" className="text-xs font-bold text-slate-700 block mb-1">
                  N° Colegiatura (CMP) <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="doc-cmp"
                  type="text"
                  placeholder="CMP-54321"
                  value={form.cmp}
                  aria-invalid={!!fieldErrors.cmp}
                  aria-describedby={fieldErrors.cmp ? 'err-doc-cmp' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, cmp: e.target.value });
                    if (fieldErrors.cmp) setFieldErrors({ ...fieldErrors, cmp: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm font-mono transition-all outline-none ${
                    fieldErrors.cmp
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.cmp && (
                  <p id="err-doc-cmp" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.cmp}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="doc-esp" className="text-xs font-bold text-slate-700 block mb-1">
                  Especialidad Médica <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <select
                  id="doc-esp"
                  value={form.especialidadId}
                  aria-invalid={!!fieldErrors.especialidadId}
                  aria-describedby={fieldErrors.especialidadId ? 'err-doc-esp' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, especialidadId: e.target.value });
                    if (fieldErrors.especialidadId) setFieldErrors({ ...fieldErrors, especialidadId: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.especialidadId
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                >
                  <option value="">Selecciona especialidad...</option>
                  {especialidades.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.nombre}
                    </option>
                  ))}
                </select>
                {fieldErrors.especialidadId && (
                  <p id="err-doc-esp" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.especialidadId}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="doc-tel" className="text-xs font-bold text-slate-700 block mb-1">
                  Teléfono de Contacto <span className="text-rose-600">*</span>
                </label>
                <input
                  id="doc-tel"
                  type="tel"
                  placeholder="987654321 (9 dígitos)"
                  value={form.telefono}
                  aria-invalid={!!fieldErrors.telefono}
                  aria-describedby={fieldErrors.telefono ? 'err-doc-tel' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, telefono: e.target.value });
                    if (fieldErrors.telefono) setFieldErrors({ ...fieldErrors, telefono: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.telefono
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.telefono && (
                  <p id="err-doc-tel" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.telefono}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="doc-correo" className="text-xs font-bold text-slate-700 block mb-1">
                  Correo Electrónico <span className="text-rose-600">*</span>
                </label>
                <input
                  id="doc-correo"
                  type="email"
                  placeholder="medico@clinitech.com"
                  value={form.correo}
                  aria-invalid={!!fieldErrors.correo}
                  aria-describedby={fieldErrors.correo ? 'err-doc-correo' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, correo: e.target.value });
                    if (fieldErrors.correo) setFieldErrors({ ...fieldErrors, correo: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.correo
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.correo && (
                  <p id="err-doc-correo" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.correo}
                  </p>
                )}
              </div>

              <div className="md:col-span-2 lg:col-span-3">
                <label htmlFor="doc-horario" className="text-xs font-bold text-slate-700 block mb-1">
                  Horario de Atención Habitual <span className="text-rose-600">*</span>
                </label>
                <input
                  id="doc-horario"
                  type="text"
                  placeholder="08:00 - 16:00"
                  value={form.horarioAtencion}
                  aria-invalid={!!fieldErrors.horarioAtencion}
                  aria-describedby={fieldErrors.horarioAtencion ? 'err-doc-horario' : undefined}
                  onChange={(e) => {
                    setForm({ ...form, horarioAtencion: e.target.value });
                    if (fieldErrors.horarioAtencion) setFieldErrors({ ...fieldErrors, horarioAtencion: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    fieldErrors.horarioAtencion
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {fieldErrors.horarioAtencion && (
                  <p id="err-doc-horario" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {fieldErrors.horarioAtencion}
                  </p>
                )}
              </div>

              {/* Casilla para generar usuario de acceso */}
              <div className="md:col-span-2 lg:col-span-3 pt-3 border-t border-slate-200">
                <label className="flex items-center gap-3 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={crearCuenta}
                    onChange={(e) => {
                      setCrearCuenta(e.target.checked);
                      if (!e.target.checked) {
                        setFieldErrors((prev) => {
                          const copy = { ...prev };
                          delete copy.cuentaUser;
                          delete copy.cuentaPass;
                          return copy;
                        });
                      }
                    }}
                    className="w-4 h-4 rounded text-[#006591] focus:ring-[#006591]"
                  />
                  <span className="text-xs font-bold text-slate-800">
                    Crear credenciales de usuario médico para acceso al sistema
                  </span>
                </label>

                {crearCuenta && (
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-3 p-4 bg-sky-50/60 border border-sky-200 rounded-xl animate-in fade-in duration-150">
                    <div>
                      <label htmlFor="med-acc-user" className="text-xs font-bold text-slate-700 block mb-1">
                        Nombre de Usuario Institucional <span className="text-rose-600">*</span>
                      </label>
                      <input
                        id="med-acc-user"
                        type="text"
                        placeholder="ej. dr.roberto"
                        value={cuentaUser}
                        aria-invalid={!!fieldErrors.cuentaUser}
                        aria-describedby={fieldErrors.cuentaUser ? 'err-med-user' : undefined}
                        onChange={(e) => {
                          setCuentaUser(e.target.value);
                          if (fieldErrors.cuentaUser) setFieldErrors({ ...fieldErrors, cuentaUser: undefined });
                        }}
                        className={`w-full px-3 py-2 rounded-xl text-xs transition-all outline-none ${
                          fieldErrors.cuentaUser
                            ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                            : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                        }`}
                      />
                      {fieldErrors.cuentaUser && (
                        <p id="err-med-user" className="mt-1 text-[11px] text-rose-600 font-semibold flex items-center gap-1">
                          <span className="material-symbols-outlined text-[13px] shrink-0">error</span>
                          {fieldErrors.cuentaUser}
                        </p>
                      )}
                    </div>
                    <div>
                      <label htmlFor="med-acc-pass" className="text-xs font-bold text-slate-700 block mb-1">
                        Contraseña de Acceso <span className="text-rose-600">*</span>
                      </label>
                      <input
                        id="med-acc-pass"
                        type="password"
                        placeholder="Mínimo 6 caracteres"
                        value={cuentaPass}
                        aria-invalid={!!fieldErrors.cuentaPass}
                        aria-describedby={fieldErrors.cuentaPass ? 'err-med-pass' : undefined}
                        onChange={(e) => {
                          setCuentaPass(e.target.value);
                          if (fieldErrors.cuentaPass) setFieldErrors({ ...fieldErrors, cuentaPass: undefined });
                        }}
                        className={`w-full px-3 py-2 rounded-xl text-xs transition-all outline-none ${
                          fieldErrors.cuentaPass
                            ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                            : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                        }`}
                      />
                      {fieldErrors.cuentaPass && (
                        <p id="err-med-pass" className="mt-1 text-[11px] text-rose-600 font-semibold flex items-center gap-1">
                          <span className="material-symbols-outlined text-[13px] shrink-0">error</span>
                          {fieldErrors.cuentaPass}
                        </p>
                      )}
                    </div>
                  </div>
                )}
              </div>

              <div className="md:col-span-2 lg:col-span-3 flex justify-end pt-2">
                <button
                  type="submit"
                  disabled={guardandoMedico}
                  className="bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-7 py-3 rounded-xl text-sm font-bold transition-all shadow-sm flex items-center gap-2 min-h-[44px]"
                >
                  <span className="material-symbols-outlined text-[20px]">person_add</span>
                  {guardandoMedico ? 'Registrando...' : 'Registrar Nuevo Médico'}
                </button>
              </div>
            </form>
          </section>

          {/* Tabla de Médicos Registrados */}
          <section aria-labelledby="tabla-medicos-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-6 border-b border-slate-200 flex items-center justify-between">
              <div>
                <h2 id="tabla-medicos-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <span className="material-symbols-outlined text-[#006591]">badge</span>
                  Plantel Médico del Centro Clínico
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">Especialistas disponibles para asignación y triaje.</p>
              </div>
              <span className="text-xs font-bold text-slate-600 px-3 py-1 bg-slate-100 rounded-full">
                {medicos.length} médicos
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm border-collapse">
                <caption className="sr-only">Listado de médicos activos y especialistas</caption>
                <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
                  <tr>
                    <th scope="col" className="py-3 px-4">Médico</th>
                    <th scope="col" className="py-3 px-4">Especialidad</th>
                    <th scope="col" className="py-3 px-4">CMP</th>
                    <th scope="col" className="py-3 px-4">Cuenta de Acceso</th>
                    <th scope="col" className="py-3 px-4">Teléfono</th>
                    <th scope="col" className="py-3 px-4">Horario</th>
                    <th scope="col" className="py-3 px-4 text-right">Acción</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {loading ? (
                    <tr>
                      <td colSpan="7" className="py-8 text-center text-xs text-slate-500">
                        Cargando catálogo médico...
                      </td>
                    </tr>
                  ) : medicos.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="py-10 text-center text-xs text-slate-500">
                        No hay médicos registrados actualmente.
                      </td>
                    </tr>
                  ) : (
                    medicos.map((m) => {
                      const userAcc = usuariosMedicos.find((u) => u.medicoId === m.id);
                      return (
                        <tr key={m.id} className="hover:bg-slate-50 transition-colors">
                          <td className="py-3.5 px-4 font-bold text-slate-900 flex items-center gap-3">
                            <div className="w-9 h-9 rounded-xl bg-sky-100 text-[#006591] flex items-center justify-center font-bold text-xs shrink-0">
                              {m.nombres.substring(0, 1)}
                              {m.apellidos.substring(0, 1)}
                            </div>
                            <div>
                              <span className="block font-bold leading-tight">{m.nombreCompleto || `${m.nombres} ${m.apellidos}`}</span>
                              <span className="text-[11px] text-slate-500 font-normal">{m.correo || '-'}</span>
                            </div>
                          </td>
                          <td className="py-3.5 px-4">
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-sky-50 text-[#006591] border border-sky-200">
                              {m.especialidad?.nombre || 'General'}
                            </span>
                          </td>
                          <td className="py-3.5 px-4 font-mono text-xs font-bold text-slate-700">{m.cmp}</td>
                          <td className="py-3.5 px-4">
                            {userAcc ? (
                              <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
                                <span className="material-symbols-outlined text-[15px] text-emerald-600">verified_user</span>
                                <span>@{userAcc.username}</span>
                                <span
                                  className={`w-1.5 h-1.5 rounded-full ${userAcc.activo ? 'bg-emerald-500' : 'bg-slate-400'}`}
                                  title={userAcc.activo ? 'Cuenta activa' : 'Cuenta inactiva'}
                                ></span>
                              </div>
                            ) : (
                              <button
                                type="button"
                                onClick={() => {
                                  setMedicoParaUsuario(m);
                                  setShowAssignUserModal(true);
                                  const primerNombre = (m.nombres || '').split(' ')[0].toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
                                  const primerApellido = (m.apellidos || '').split(' ')[0].toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
                                  setAssignUser(`${primerNombre}.${primerApellido}`);
                                  setAssignPass('');
                                  setAssignErrors({});
                                }}
                                className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-bold bg-amber-50 text-amber-900 border border-amber-300 hover:bg-amber-100 transition-colors shadow-2xs cursor-pointer min-h-[32px]"
                                title="Crear credenciales de acceso institucional para este médico"
                              >
                                <span className="material-symbols-outlined text-[15px] text-amber-700">key</span>
                                <span>+ Crear credenciales</span>
                              </button>
                            )}
                          </td>
                          <td className="py-3.5 px-4 text-xs text-slate-600">{m.telefono || '-'}</td>
                          <td className="py-3.5 px-4 text-xs text-slate-600">{m.horarioAtencion}</td>
                          <td className="py-3.5 px-4 text-right">
                            <button
                              onClick={() => handleDesactivarMedico(m.id, `${m.nombres} ${m.apellidos}`)}
                              aria-label={`Desactivar médico ${m.nombres} ${m.apellidos}`}
                              className="px-3 py-1.5 text-xs font-bold text-rose-700 bg-rose-50 hover:bg-rose-100 rounded-lg transition-colors min-h-[36px]"
                            >
                              Desactivar
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      )}

      {/* PESTAÑA 2: GESTIÓN DE ESPECIALIDADES Y DERIVACIONES */}
      {tab === 'especialidades' && (
        <div id="panel-especialidades" role="tabpanel" aria-labelledby="tab-especialidades" className="space-y-6">
          {/* Formulario nueva especialidad */}
          <section aria-labelledby="nueva-esp-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-4">
            <div className="pb-3 border-b border-slate-200">
              <h2 id="nueva-esp-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006591]">domain</span>
                Registrar Nueva Especialidad Médica
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">Expande la oferta clínica del centro médico.</p>
            </div>

            <form onSubmit={handleSubmitEspecialidad} className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              <div>
                <label htmlFor="nueva-esp-nombre" className="text-xs font-bold text-slate-700 block mb-1">
                  Nombre de la Especialidad <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="nueva-esp-nombre"
                  type="text"
                  required
                  placeholder="Ej. Neurología, Dermatología..."
                  value={nuevaEsp.nombre}
                  onChange={(e) => setNuevaEsp({ ...nuevaEsp, nombre: e.target.value })}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div>
                <label htmlFor="nueva-esp-desc" className="text-xs font-bold text-slate-700 block mb-1">
                  Descripción Clínica
                </label>
                <input
                  id="nueva-esp-desc"
                  type="text"
                  placeholder="Ej. Diagnóstico y tratamiento del sistema nervioso..."
                  value={nuevaEsp.descripcion}
                  onChange={(e) => setNuevaEsp({ ...nuevaEsp, descripcion: e.target.value })}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div>
                <button
                  type="submit"
                  disabled={guardandoEsp}
                  className="w-full bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all shadow-sm flex items-center justify-center gap-2 min-h-[44px]"
                >
                  <span className="material-symbols-outlined text-[18px]">add_circle</span>
                  {guardandoEsp ? 'Guardando...' : 'Registrar Especialidad Médica'}
                </button>
              </div>
            </form>
          </section>

          {/* Simulador de Ruta Óptima de Derivación e Interconsulta */}
          <section aria-labelledby="derivacion-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-5">
            <div className="pb-3 border-b border-slate-200">
              <h2 id="derivacion-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006591]">route</span>
                Ruta Óptima de Derivación e Interconsulta Médica
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Calcula la secuencia asistencial recomendada para derivar a un paciente entre servicios hospitalarios.
              </p>
            </div>

            <form onSubmit={handleConsultarRuta} className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end">
              <div>
                <label htmlFor="esp-origen-select" className="text-xs font-bold text-slate-700 block mb-1">
                  Especialidad Origen <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <select
                  id="esp-origen-select"
                  required
                  value={origenId}
                  onChange={(e) => setOrigenId(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                >
                  <option value="">Selecciona origen...</option>
                  {especialidades.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="esp-destino-select" className="text-xs font-bold text-slate-700 block mb-1">
                  Especialidad Destino <span className="text-rose-600" aria-hidden="true">*</span>
                </label>
                <select
                  id="esp-destino-select"
                  required
                  value={destinoId}
                  onChange={(e) => setDestinoId(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-xl text-sm bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                >
                  <option value="">Selecciona destino...</option>
                  {especialidades.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <button
                  type="submit"
                  disabled={loadingGrafo}
                  className="w-full bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all shadow-sm flex items-center justify-center gap-2 min-h-[44px]"
                >
                  <span className="material-symbols-outlined text-[18px]">alt_route</span>
                  {loadingGrafo ? 'Calculando ruta...' : 'Calcular Ruta de Derivación'}
                </button>
              </div>
            </form>

            {/* Visualización de la Ruta de Derivación */}
            {rutaInterconsulta && (
              <div className="p-5 bg-gradient-to-r from-sky-50 to-blue-50/80 border border-sky-200 rounded-2xl space-y-3 animate-in fade-in duration-150">
                <span className="text-xs font-extrabold text-[#006591] uppercase tracking-wider block">
                  Secuencia Asistencial de Interconsulta:
                </span>
                {rutaInterconsulta.length === 0 ? (
                  <p className="text-xs text-rose-700 font-semibold">
                    No existe ruta de interconsulta configurada entre estas dos especialidades en el centro médico.
                  </p>
                ) : (
                  <div className="flex flex-wrap items-center gap-2.5 pt-1">
                    {rutaInterconsulta.map((esp, idx) => (
                      <div key={esp.id} className="flex items-center gap-2.5">
                        <div className="px-4 py-2 bg-white text-[#006591] font-bold text-xs rounded-xl shadow-xs border border-sky-300 flex items-center gap-2">
                          <span className="w-5 h-5 rounded-full bg-sky-100 text-[#006591] flex items-center justify-center text-[10px]">
                            {idx + 1}
                          </span>
                          <span>{esp.nombre}</span>
                        </div>
                        {idx < rutaInterconsulta.length - 1 && (
                          <span className="material-symbols-outlined text-[#006591] text-[20px]" aria-hidden="true">
                            arrow_right_alt
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </section>

          {/* Tabla de Especialidades Registradas */}
          <section aria-labelledby="cat-esp-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-6 border-b border-slate-200 flex items-center justify-between">
              <div>
                <h2 id="cat-esp-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <span className="material-symbols-outlined text-[#006591]">view_list</span>
                  Catálogo de Especialidades Activas
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">Servicios clínicos y sus interconexiones de atención.</p>
              </div>
              <span className="text-xs font-bold text-slate-600 px-3 py-1 bg-slate-100 rounded-full">
                {especialidades.length} especialidades
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm border-collapse">
                <caption className="sr-only">Catálogo de especialidades del centro de salud</caption>
                <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
                  <tr>
                    <th scope="col" className="py-3 px-4">ID</th>
                    <th scope="col" className="py-3 px-4">Especialidad</th>
                    <th scope="col" className="py-3 px-4">Descripción</th>
                    <th scope="col" className="py-3 px-4 text-right">Especialidades Relacionadas</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {especialidades.map((e) => (
                    <tr key={e.id} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3.5 px-4 font-mono font-bold text-xs text-slate-400">#{e.id}</td>
                      <td className="py-3.5 px-4 font-bold text-slate-900">{e.nombre}</td>
                      <td className="py-3.5 px-4 text-xs text-slate-600">{e.descripcion || 'Sin descripción'}</td>
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={() => handleConsultarRelacionadas(e.id)}
                          aria-label={`Ver conexiones clínicas de ${e.nombre}`}
                          className="px-3.5 py-1.5 text-xs bg-sky-50 text-[#006591] hover:bg-[#006591] hover:text-white rounded-xl font-bold transition-all shadow-2xs min-h-[36px]"
                        >
                          Ver Especialidades Vinculadas
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Modal/Panel de Especialidades Relacionadas */}
            {relacionadas && (
              <div className="p-5 bg-slate-50 border-t border-slate-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-xs font-bold text-slate-700">
                    Especialidades con interconsulta directa:
                  </span>
                  {relacionadas.length === 0 ? (
                    <span className="text-xs text-slate-500 italic">Sin derivaciones configuradas</span>
                  ) : (
                    relacionadas.map((r) => (
                      <span
                        key={r.id}
                        className="px-2.5 py-1 bg-white border border-slate-300 rounded-lg text-xs font-semibold text-[#006591] shadow-2xs"
                      >
                        {r.nombre}
                      </span>
                    ))
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => setRelacionadas(null)}
                  className="text-xs font-semibold text-slate-600 hover:text-slate-900 px-3 py-1 bg-white rounded-lg border border-slate-200 self-start cursor-pointer"
                >
                  Cerrar
                </button>
              </div>
            )}
          </section>
        </div>
      )}

      {/* Modal para Crear y Asignar Credenciales Institucionales a Médico Existente */}
      {showAssignUserModal && medicoParaUsuario && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-asignar-usuario-title"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-150"
        >
          <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-md p-6 space-y-5">
            <div className="flex items-start justify-between pb-3 border-b border-slate-200">
              <div>
                <h3 id="modal-asignar-usuario-title" className="text-base font-bold text-slate-900 flex items-center gap-2">
                  <span className="material-symbols-outlined text-[#006591]">key</span>
                  Crear Credenciales de Acceso
                </h3>
                <p className="text-xs text-slate-600 mt-1">
                  Para el Dr(a). <span className="font-bold text-slate-800">{medicoParaUsuario.nombres} {medicoParaUsuario.apellidos}</span>
                </p>
                <p className="text-[11px] text-slate-400 font-mono mt-0.5">CMP: {medicoParaUsuario.cmp}</p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setShowAssignUserModal(false);
                  setMedicoParaUsuario(null);
                  setAssignErrors({});
                }}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg transition-colors cursor-pointer"
                aria-label="Cerrar modal"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            {assignErrors.general && (
              <div role="alert" className="p-3 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl text-xs text-rose-800 font-medium">
                {assignErrors.general}
              </div>
            )}

            <form noValidate onSubmit={handleAsignarUsuario} className="space-y-4">
              <div>
                <label htmlFor="modal-assign-user" className="text-xs font-bold text-slate-700 block mb-1">
                  Nombre de Usuario Institucional <span className="text-rose-600">*</span>
                </label>
                <input
                  id="modal-assign-user"
                  type="text"
                  placeholder="ej. cristiano.ronaldo"
                  value={assignUser}
                  aria-invalid={!!assignErrors.assignUser}
                  aria-describedby={assignErrors.assignUser ? 'err-assign-user' : undefined}
                  onChange={(e) => {
                    setAssignUser(e.target.value);
                    if (assignErrors.assignUser) setAssignErrors({ ...assignErrors, assignUser: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    assignErrors.assignUser
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {assignErrors.assignUser && (
                  <p id="err-assign-user" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {assignErrors.assignUser}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="modal-assign-pass" className="text-xs font-bold text-slate-700 block mb-1">
                  Contraseña de Acceso <span className="text-rose-600">*</span>
                </label>
                <input
                  id="modal-assign-pass"
                  type="password"
                  placeholder="Mínimo 6 caracteres"
                  value={assignPass}
                  aria-invalid={!!assignErrors.assignPass}
                  aria-describedby={assignErrors.assignPass ? 'err-assign-pass' : undefined}
                  onChange={(e) => {
                    setAssignPass(e.target.value);
                    if (assignErrors.assignPass) setAssignErrors({ ...assignErrors, assignPass: undefined });
                  }}
                  className={`w-full px-3.5 py-2.5 rounded-xl text-sm transition-all outline-none ${
                    assignErrors.assignPass
                      ? 'border-2 border-rose-500 bg-rose-50/20 text-slate-900 focus:border-rose-600 focus:ring-2 focus:ring-rose-500/20 shadow-xs'
                      : 'border border-slate-300 bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20'
                  }`}
                />
                {assignErrors.assignPass && (
                  <p id="err-assign-pass" className="mt-1.5 text-xs text-rose-600 font-semibold flex items-center gap-1">
                    <span className="material-symbols-outlined text-[15px] shrink-0">error</span>
                    {assignErrors.assignPass}
                  </p>
                )}
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => {
                    setShowAssignUserModal(false);
                    setMedicoParaUsuario(null);
                    setAssignErrors({});
                  }}
                  className="px-4 py-2.5 text-xs font-bold text-slate-600 hover:text-slate-800 bg-slate-100 hover:bg-slate-200 rounded-xl transition-all cursor-pointer min-h-[40px]"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={asignando}
                  className="px-5 py-2.5 text-xs font-bold text-white bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 rounded-xl shadow-xs transition-all flex items-center gap-1.5 min-h-[40px] cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[16px]">check</span>
                  {asignando ? 'Creando credenciales...' : 'Crear y Vincular Credenciales'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}