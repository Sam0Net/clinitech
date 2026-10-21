import { useState, useEffect } from 'react';
import { api } from '../api';

export default function UsersManagement() {
  const currentUser = JSON.parse(localStorage.getItem('clinitech_user') || '{}');

  const [usuarios, setUsuarios] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filtros
  const [q, setQ] = useState('');
  const [rolFiltro, setRolFiltro] = useState('ALL');

  // Modal Crear Usuario
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [createForm, setCreateForm] = useState({
    username: '',
    password: '',
    rol: 'RECEPCIONISTA',
    medicoId: '',
  });

  // Modal Resetear Contraseña
  const [showResetModal, setShowResetModal] = useState(false);
  const [userToReset, setUserToReset] = useState(null);
  const [newPassword, setNewPassword] = useState('');
  const [reseteando, setReseteando] = useState(false);

  // Modal Eliminar Usuario
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      setError('');
      const params = new URLSearchParams();
      if (q.trim()) params.append('q', q.trim());
      if (rolFiltro !== 'ALL') params.append('rol', rolFiltro);

      const [usersData, medsData] = await Promise.all([
        api(`/api/auth/usuarios?${params.toString()}`),
        api('/api/medicos').catch(() => []),
      ]);

      setUsuarios(Array.isArray(usersData) ? usersData : []);
      setMedicos(Array.isArray(medsData) ? medsData : (medsData?.content || []));
    } catch (err) {
      setError(err.message || 'Error al cargar catálogo de usuarios.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, [rolFiltro]);

  const handleBuscar = (e) => {
    e.preventDefault();
    cargarDatos();
  };

  const handleCrearUsuario = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setGuardando(true);
    try {
      const payload = {
        username: createForm.username.trim(),
        password: createForm.password,
        rol: createForm.rol,
        medicoId: createForm.rol === 'MEDICO' && createForm.medicoId ? Number(createForm.medicoId) : null,
        pacienteId: null,
      };

      await api('/api/auth/usuarios', {
        method: 'POST',
        body: payload,
      });

      setSuccess(`Cuenta institucional para "${createForm.username}" creada exitosamente con perfil ${createForm.rol}.`);
      setShowCreateModal(false);
      setCreateForm({
        username: '',
        password: '',
        rol: 'RECEPCIONISTA',
        medicoId: '',
      });
      cargarDatos();
    } catch (err) {
      setError(err.message || 'No se pudo crear la cuenta de usuario.');
    } finally {
      setGuardando(false);
    }
  };

  const handleToggleEstado = async (u) => {
    if (u.id === currentUser.userId) {
      setError('No puedes desactivar tu propia cuenta activa.');
      return;
    }
    const nuevoEstado = !u.activo;
    const confirmMsg = nuevoEstado
      ? `¿Deseas reactivar el acceso de "${u.username}"?`
      : `¿Estás seguro de inhabilitar el acceso de "${u.username}"? No podrá iniciar sesión hasta que sea reactivado.`;

    if (!window.confirm(confirmMsg)) return;

    setError('');
    setSuccess('');
    try {
      await api(`/api/auth/usuarios/${u.id}/estado`, {
        method: 'PATCH',
        body: { activo: nuevoEstado },
      });
      setSuccess(`El estado de "${u.username}" fue actualizado a ${nuevoEstado ? 'Activo' : 'Inactivo'}.`);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'No se pudo actualizar el estado del usuario.');
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (!userToReset) return;
    setError('');
    setSuccess('');
    setReseteando(true);
    try {
      await api(`/api/auth/usuarios/${userToReset.id}/password`, {
        method: 'PATCH',
        body: { password: newPassword },
      });
      setSuccess(`Contraseña restablecida con éxito para el usuario "${userToReset.username}".`);
      setShowResetModal(false);
      setUserToReset(null);
      setNewPassword('');
    } catch (err) {
      setError(err.message || 'No se pudo restablecer la contraseña.');
    } finally {
      setReseteando(false);
    }
  };

  const handleEliminarUsuario = async () => {
    if (!userToDelete) return;
    if (userToDelete.id === currentUser.userId) {
      setError('No puedes eliminar tu propia cuenta activa.');
      setShowDeleteModal(false);
      return;
    }
    setError('');
    setSuccess('');
    setEliminando(true);
    try {
      await api(`/api/auth/usuarios/${userToDelete.id}`, {
        method: 'DELETE',
      });
      setSuccess(`Usuario "${userToDelete.username}" eliminado definitivamente del sistema.`);
      setShowDeleteModal(false);
      setUserToDelete(null);
      cargarDatos();
    } catch (err) {
      setError(err.message || 'No se pudo eliminar el usuario.');
    } finally {
      setEliminando(false);
    }
  };

  // Contadores para métricas
  const totalUsuarios = usuarios.length;
  const totalRecepcion = usuarios.filter((u) => u.rol === 'RECEPCIONISTA').length;
  const totalMedicos = usuarios.filter((u) => u.rol === 'MEDICO').length;
  const totalAdmins = usuarios.filter((u) => u.rol === 'ADMIN').length;

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado y Acción Principal */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Gestión de Usuarios y Accesos
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            Administración centralizada de cuentas del personal hospitalario (Recepcionistas, Médicos, Administradores).
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setError('');
            setShowCreateModal(true);
          }}
          className="self-start sm:self-auto inline-flex items-center gap-2 px-5 py-2.5 bg-[#006591] hover:bg-[#004d70] text-white rounded-xl text-xs font-bold transition-all shadow-sm active:scale-95 min-h-[44px]"
        >
          <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
            person_add
          </span>
          Nuevo Usuario Institucional
        </button>
      </div>

      {/* Alertas Accesibles */}
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

      {/* Métricas de Cuentas */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center text-slate-700">
            <span className="material-symbols-outlined text-[24px]">group</span>
          </div>
          <div>
            <span className="text-2xl font-black text-slate-900">{totalUsuarios}</span>
            <p className="text-xs text-slate-500 font-semibold">Total de Usuarios</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-700">
            <span className="material-symbols-outlined text-[24px]">support_agent</span>
          </div>
          <div>
            <span className="text-2xl font-black text-slate-900">{totalRecepcion}</span>
            <p className="text-xs text-slate-500 font-semibold">Recepcionistas</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-700">
            <span className="material-symbols-outlined text-[24px]">stethoscope</span>
          </div>
          <div>
            <span className="text-2xl font-black text-slate-900">{totalMedicos}</span>
            <p className="text-xs text-slate-500 font-semibold">Personal Médico</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-sky-50 flex items-center justify-center text-[#006591]">
            <span className="material-symbols-outlined text-[24px]">admin_panel_settings</span>
          </div>
          <div>
            <span className="text-2xl font-black text-slate-900">{totalAdmins}</span>
            <p className="text-xs text-slate-500 font-semibold">Administradores</p>
          </div>
        </div>
      </div>

      {/* Barra de Filtros y Búsqueda */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        {/* Formulario de Búsqueda */}
        <form onSubmit={handleBuscar} className="flex items-center gap-2 flex-1 max-w-md">
          <div className="relative flex-1">
            <span className="material-symbols-outlined text-slate-400 text-[18px] absolute left-3 top-1/2 -translate-y-1/2">
              search
            </span>
            <input
              type="text"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Buscar por usuario o personal..."
              className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-xl text-xs bg-slate-50 focus:bg-white text-slate-800 placeholder:text-slate-400 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-bold transition-colors"
          >
            Buscar
          </button>
        </form>

        {/* Selector de Rol */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 md:pb-0">
          <span className="text-xs font-bold text-slate-500 whitespace-nowrap">Filtrar Rol:</span>
          {[
            { id: 'ALL', label: 'Todos' },
            { id: 'RECEPCIONISTA', label: 'Recepción' },
            { id: 'MEDICO', label: 'Médicos' },
            { id: 'ADMIN', label: 'Admin' },
            { id: 'PACIENTE', label: 'Pacientes' },
          ].map((r) => (
            <button
              key={r.id}
              type="button"
              onClick={() => setRolFiltro(r.id)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap ${
                rolFiltro === r.id
                  ? 'bg-[#006591] text-white shadow-2xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tabla de Usuarios */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50/70 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                <th className="py-3 px-4">Usuario</th>
                <th className="py-3 px-4">Perfil Institucional</th>
                <th className="py-3 px-4">Vinculación</th>
                <th className="py-3 px-4">Estado</th>
                <th className="py-3 px-4">Último Acceso</th>
                <th className="py-3 px-4 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400 font-medium">
                    Cargando cuentas institucionales...
                  </td>
                </tr>
              ) : usuarios.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400 font-medium">
                    No se encontraron usuarios con los filtros especificados.
                  </td>
                </tr>
              ) : (
                usuarios.map((u) => {
                  const isCurrent = u.id === currentUser.userId;

                  const badgeColors = {
                    ADMIN: 'bg-sky-100 text-[#006591] border-sky-200',
                    MEDICO: 'bg-emerald-100 text-emerald-800 border-emerald-200',
                    RECEPCIONISTA: 'bg-indigo-100 text-indigo-800 border-indigo-200',
                    PACIENTE: 'bg-slate-100 text-slate-700 border-slate-200',
                  };

                  return (
                    <tr key={u.id} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-3.5 px-4 font-bold text-slate-900">
                        <div className="flex items-center gap-2.5">
                          <div className="w-8 h-8 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center shrink-0">
                            <span className="material-symbols-outlined text-[18px]">
                              {u.rol === 'MEDICO'
                                ? 'stethoscope'
                                : u.rol === 'RECEPCIONISTA'
                                ? 'support_agent'
                                : u.rol === 'ADMIN'
                                ? 'admin_panel_settings'
                                : 'person'}
                            </span>
                          </div>
                          <div>
                            <span className="block leading-tight font-extrabold">{u.username}</span>
                            {isCurrent && (
                              <span className="inline-block mt-0.5 px-1.5 py-0.2 bg-emerald-100 text-emerald-800 text-[9px] font-black rounded">
                                Tu cuenta activa
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                            badgeColors[u.rol] || 'bg-slate-100 text-slate-700'
                          }`}
                        >
                          {u.rol}
                        </span>
                      </td>

                      <td className="py-3.5 px-4 text-slate-600">
                        {u.medicoNombre ? (
                          <span className="font-semibold text-slate-900">Dr(a). {u.medicoNombre}</span>
                        ) : u.pacienteNombre ? (
                          <span>Paciente: {u.pacienteNombre}</span>
                        ) : (
                          <span className="text-slate-400 italic">Personal Directo</span>
                        )}
                      </td>

                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                            u.activo
                              ? 'bg-emerald-50 text-emerald-700'
                              : 'bg-rose-50 text-rose-700'
                          }`}
                        >
                          <span
                            className={`w-1.5 h-1.5 rounded-full ${
                              u.activo ? 'bg-emerald-500' : 'bg-rose-500'
                            }`}
                          />
                          {u.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>

                      <td className="py-3.5 px-4 text-slate-500 font-mono text-[11px]">
                        {u.ultimoAcceso
                          ? new Date(u.ultimoAcceso).toLocaleString('es-PE', {
                              dateStyle: 'short',
                              timeStyle: 'short',
                            })
                          : 'Sin ingresos'}
                      </td>

                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            type="button"
                            onClick={() => {
                              setUserToReset(u);
                              setNewPassword('');
                              setShowResetModal(true);
                            }}
                            title="Restablecer contraseña"
                            className="p-1.5 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            <span className="material-symbols-outlined text-[18px]">key</span>
                          </button>

                          {!isCurrent && (
                            <>
                              <button
                                type="button"
                                onClick={() => handleToggleEstado(u)}
                                className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors ${
                                  u.activo
                                    ? 'bg-amber-50 hover:bg-amber-100 text-amber-800'
                                    : 'bg-emerald-50 hover:bg-emerald-100 text-emerald-700'
                                }`}
                              >
                                {u.activo ? 'Inhabilitar' : 'Reactivar'}
                              </button>

                              <button
                                type="button"
                                onClick={() => {
                                  setUserToDelete(u);
                                  setShowDeleteModal(true);
                                }}
                                title="Eliminar usuario definitivamente"
                                className="p-1.5 text-rose-600 hover:text-rose-900 hover:bg-rose-50 rounded-lg transition-colors"
                              >
                                <span className="material-symbols-outlined text-[18px]">delete</span>
                              </button>
                            </>
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
      </div>

      {/* ============================================================== */}
      {/* MODAL: CREAR NUEVO USUARIO INSTITUCIONAL (RECEPCIONISTA, ETC) */}
      {/* ============================================================== */}
      {showCreateModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-crear-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150"
        >
          <div className="bg-white w-full max-w-md rounded-3xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#006591] to-[#0284c7] text-white flex items-center justify-center shadow-xs">
                  <span className="material-symbols-outlined text-[22px]">person_add</span>
                </div>
                <div>
                  <h2 id="modal-crear-title" className="text-base font-extrabold text-slate-900">
                    Nueva Cuenta Institucional
                  </h2>
                  <p className="text-xs text-slate-500">Crea el acceso para personal hospitalario.</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            <form onSubmit={handleCrearUsuario} className="p-6 space-y-4">
              {/* Rol */}
              <div>
                <label htmlFor="modal-rol" className="text-xs font-bold text-slate-700 block mb-1">
                  Rol del Usuario <span className="text-rose-600">*</span>
                </label>
                <select
                  id="modal-rol"
                  value={createForm.rol}
                  onChange={(e) => setCreateForm({ ...createForm, rol: e.target.value })}
                  className="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-xs bg-white text-slate-800 font-semibold focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                >
                  <option value="RECEPCIONISTA">Recepcionista (Admisión, Citas en Ventanilla)</option>
                  <option value="ADMIN">Administrador (Control total del sistema)</option>
                  <option value="MEDICO">Médico (Atención y Diagnóstico Clínico)</option>
                </select>
                <p className="text-[11px] text-slate-500 mt-1">
                  {createForm.rol === 'RECEPCIONISTA' &&
                    'Permite registrar pacientes en el ABB, gestionar citas y monitorear triaje.'}
                  {createForm.rol === 'ADMIN' &&
                    'Permite gestionar especialidades, médicos, reportes y cuentas de usuario.'}
                  {createForm.rol === 'MEDICO' &&
                    'Permite atender la cola de triaje, llamar pacientes y emitir recetas médicas.'}
                </p>
              </div>

              {/* Si es Médico, vinculación opcional */}
              {createForm.rol === 'MEDICO' && (
                <div>
                  <label htmlFor="modal-medico" className="text-xs font-bold text-slate-700 block mb-1">
                    Vincular a Ficha Médica
                  </label>
                  <select
                    id="modal-medico"
                    value={createForm.medicoId}
                    onChange={(e) => setCreateForm({ ...createForm, medicoId: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-800 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                  >
                    <option value="">-- Seleccionar médico (opcional) --</option>
                    {medicos.map((m) => (
                      <option key={m.id} value={m.id}>
                        Dr(a). {m.nombres} {m.apellidos} ({m.especialidadNombre || 'Sin esp.'})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {/* Usuario */}
              <div>
                <label htmlFor="modal-username" className="text-xs font-bold text-slate-700 block mb-1">
                  Nombre de Usuario <span className="text-rose-600">*</span>
                </label>
                <div className="relative flex items-center bg-slate-50 border border-slate-300 rounded-xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                  <span className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none">
                    person
                  </span>
                  <input
                    id="modal-username"
                    type="text"
                    required
                    minLength={4}
                    placeholder="Ej. recepcion_central, admin_turno"
                    value={createForm.username}
                    onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
                    className="w-full bg-transparent px-3 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                  />
                </div>
              </div>

              {/* Contraseña */}
              <div>
                <label htmlFor="modal-password" className="text-xs font-bold text-slate-700 block mb-1">
                  Contraseña Inicial <span className="text-rose-600">*</span>
                </label>
                <div className="relative flex items-center bg-slate-50 border border-slate-300 rounded-xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                  <span className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none">
                    lock
                  </span>
                  <input
                    id="modal-password"
                    type="password"
                    required
                    minLength={6}
                    placeholder="Mínimo 6 caracteres"
                    value={createForm.password}
                    onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                    className="w-full bg-transparent px-3 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                  />
                </div>
              </div>

              {/* Botones de Acción */}
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-100 rounded-xl text-xs font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={guardando}
                  className="px-5 py-2 bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white rounded-xl text-xs font-bold transition-all shadow-xs flex items-center gap-1.5"
                >
                  {guardando ? 'Creando cuenta...' : 'Crear Cuenta Institucional'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* MODAL: RESETEAR CONTRASEÑA                                     */}
      {/* ============================================================== */}
      {showResetModal && userToReset && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-reset-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150"
        >
          <div className="bg-white w-full max-w-sm rounded-3xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-amber-500 text-white flex items-center justify-center shadow-xs">
                  <span className="material-symbols-outlined text-[20px]">key</span>
                </div>
                <div>
                  <h2 id="modal-reset-title" className="text-sm font-extrabold text-slate-900">
                    Cambiar Contraseña
                  </h2>
                  <p className="text-[11px] text-slate-500">Usuario: @{userToReset.username}</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowResetModal(false)}
                className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
              >
                <span className="material-symbols-outlined text-[18px]">close</span>
              </button>
            </div>

            <form onSubmit={handleResetPassword} className="p-6 space-y-4">
              <div>
                <label htmlFor="reset-new-pass" className="text-xs font-bold text-slate-700 block mb-1">
                  Nueva Contraseña <span className="text-rose-600">*</span>
                </label>
                <input
                  id="reset-new-pass"
                  type="password"
                  required
                  minLength={6}
                  placeholder="Mínimo 6 caracteres"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white text-slate-900 focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-2">
                <button
                  type="button"
                  onClick={() => setShowResetModal(false)}
                  className="px-3.5 py-2 border border-slate-300 text-slate-700 hover:bg-slate-100 rounded-xl text-xs font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={reseteando}
                  className="px-4 py-2 bg-[#006591] hover:bg-[#004d70] disabled:opacity-60 text-white rounded-xl text-xs font-bold transition-all shadow-xs"
                >
                  {reseteando ? 'Actualizando...' : 'Actualizar Clave'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* MODAL: ELIMINAR USUARIO DEFINITIVAMENTE                        */}
      {/* ============================================================== */}
      {showDeleteModal && userToDelete && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-delete-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150"
        >
          <div className="bg-white w-full max-w-md rounded-3xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center shadow-xs">
                  <span className="material-symbols-outlined text-[22px]">delete_forever</span>
                </div>
                <div>
                  <h2 id="modal-delete-title" className="text-sm font-extrabold text-slate-900">
                    Eliminar Usuario
                  </h2>
                  <p className="text-[11px] text-slate-500">Acción permanente e irreversible</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => {
                  setShowDeleteModal(false);
                  setUserToDelete(null);
                }}
                className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
              >
                <span className="material-symbols-outlined text-[18px]">close</span>
              </button>
            </div>

            <div className="p-6 space-y-4">
              <p className="text-xs text-slate-600 leading-relaxed">
                ¿Estás seguro de que deseas eliminar permanentemente la cuenta de usuario{' '}
                <strong className="text-slate-900 font-extrabold">@{userToDelete.username}</strong>{' '}
                con rol <strong className="text-slate-900 font-bold">{userToDelete.rol}</strong>?
              </p>

              <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-2xl flex items-start gap-2.5">
                <span className="material-symbols-outlined text-rose-600 text-[18px] shrink-0 mt-0.5">warning</span>
                <p className="text-[11px] text-rose-800 leading-tight">
                  Esta acción no se puede deshacer. Las credenciales de acceso serán eliminadas por completo del sistema.
                </p>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowDeleteModal(false);
                    setUserToDelete(null);
                  }}
                  className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-100 rounded-xl text-xs font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  disabled={eliminando}
                  onClick={handleEliminarUsuario}
                  className="px-4 py-2 bg-rose-600 hover:bg-rose-700 disabled:opacity-60 text-white rounded-xl text-xs font-bold transition-all shadow-xs flex items-center gap-1.5"
                >
                  {eliminando ? (
                    'Eliminando...'
                  ) : (
                    <>
                      <span className="material-symbols-outlined text-[16px]">delete</span>
                      Eliminar definitivamente
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
