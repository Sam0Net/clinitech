import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';

export default function Login() {
  const navigate = useNavigate();
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Formulario de autoregistro de pacientes
  const [regForm, setRegForm] = useState({
    username: '',
    password: '',
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

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      // El backend ahora reconoce automáticamente el rol según las credenciales del usuario
      const data = await api('/api/auth/login', {
        method: 'POST',
        body: { username, password },
      });
      localStorage.setItem('clinitech_token', data.accessToken);
      const me = await api('/api/auth/me').catch(() => ({}));
      localStorage.setItem(
        'clinitech_user',
        JSON.stringify({ ...data, medicoId: me.medicoId, pacienteId: me.pacienteId })
      );
      if (data.role === 'PACIENTE') {
        navigate('/appointments');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      const msg = err.message || '';
      if (
        msg.includes('401') ||
        msg.toLowerCase().includes('unauthorized') ||
        msg.toLowerCase().includes('credenciales') ||
        msg.toLowerCase().includes('incorrect')
      ) {
        setError('El usuario o la contraseña ingresados son incorrectos. Por favor, verifica tus datos e inténtalo nuevamente.');
      } else {
        setError(msg || 'El usuario o la contraseña son incorrectos. Por favor, verifica tus datos.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegistro = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await api('/api/auth/registro', {
        method: 'POST',
        body: regForm,
      });
      localStorage.setItem('clinitech_token', data.accessToken);
      const me = await api('/api/auth/me').catch(() => ({}));
      localStorage.setItem(
        'clinitech_user',
        JSON.stringify({ ...data, medicoId: me.medicoId, pacienteId: me.pacienteId })
      );
      navigate('/appointments');
    } catch (err) {
      setError(err.message || 'Error al completar el registro.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-[#edf2f7] p-3 sm:p-6 lg:p-8 relative overflow-hidden font-sans">
      {/* Fondos ambientales sutiles */}
      <div className="absolute inset-0 pointer-events-none" aria-hidden="true">
        <div className="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-sky-200/50 blur-3xl" />
        <div className="absolute -bottom-32 -right-32 w-96 h-96 rounded-full bg-blue-200/40 blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] rounded-full bg-slate-200/40 blur-3xl -z-10" />
      </div>

      {/* Tarjeta Split Card Principal con dimensiones simétricas y fijas en escritorio */}
      <main className="w-full max-w-[1020px] md:h-[620px] bg-white rounded-[28px] sm:rounded-[36px] shadow-[0_20px_60px_-15px_rgba(15,23,42,0.18)] border border-slate-200/90 p-2.5 sm:p-3 relative z-10 overflow-hidden flex flex-col justify-center">
        <div
          className={`flex flex-col md:flex-row items-stretch h-full w-full transition-all duration-500 ease-in-out ${
            isRegistering ? 'md:flex-row-reverse' : ''
          }`}
        >
          {/* ============================================================== */}
          {/* PANEL FORMULARIO (Izquierda en Login, Derecha en Registro)    */}
          {/* ============================================================== */}
          <div className="w-full md:w-1/2 h-full p-6 sm:p-8 lg:p-10 flex flex-col justify-between overflow-y-auto">
            {/* Header de Marca CliniTech */}
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-[#006591] to-[#0284c7] flex items-center justify-center text-white shadow-sm shrink-0">
                <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
                  local_hospital
                </span>
              </div>
              <div>
                <span className="text-xl font-extrabold tracking-tight text-slate-900 block leading-none">
                  CliniTech
                </span>
                <span className="text-[10px] uppercase font-bold tracking-widest text-[#006591]">
                  Salud Digital
                </span>
              </div>
            </div>

            {/* Mensaje de Error Accesible */}
            {error && (
              <div
                role="alert"
                aria-live="polite"
                className="my-2 p-3 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl flex items-start gap-2.5 text-xs text-rose-800 font-medium shadow-2xs"
              >
                <span
                  className="material-symbols-outlined text-rose-600 text-[18px] shrink-0 mt-0.5"
                  aria-hidden="true"
                >
                  error
                </span>
                <span>{error}</span>
              </div>
            )}

            {/* ---------------------------------------------------------- */}
            {/* VISTA 1: FORMULARIO DE INICIO DE SESIÓN                    */}
            {/* ---------------------------------------------------------- */}
            {!isRegistering ? (
              <div className="my-auto py-4 flex flex-col gap-6">
                <div>
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                    Iniciar Sesión
                  </h1>
                  <p className="text-xs text-slate-500 font-medium mt-1">
                    Ingresa tus credenciales para acceder a la plataforma médica.
                  </p>
                </div>

                <form onSubmit={handleLogin} className="flex flex-col gap-4">
                  {/* Campo de Usuario */}
                  <div className="flex flex-col gap-1">
                    <label
                      htmlFor="login-username"
                      className="text-xs font-bold text-slate-700"
                    >
                      Nombre de Usuario
                    </label>
                    <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                      <span
                        className="material-symbols-outlined text-slate-400 text-[20px] pl-3.5 pointer-events-none select-none"
                        aria-hidden="true"
                      >
                        person
                      </span>
                      <input
                        id="login-username"
                        type="text"
                        required
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="Ingresa tu usuario institucional"
                        className="w-full bg-transparent px-3 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                      />
                    </div>
                  </div>

                  {/* Campo de Contraseña */}
                  <div className="flex flex-col gap-1">
                    <label
                      htmlFor="login-password"
                      className="text-xs font-bold text-slate-700"
                    >
                      Contraseña de Acceso
                    </label>
                    <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                      <span
                        className="material-symbols-outlined text-slate-400 text-[20px] pl-3.5 pointer-events-none select-none"
                        aria-hidden="true"
                      >
                        lock
                      </span>
                      <input
                        id="login-password"
                        type={showPassword ? 'text' : 'password'}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full bg-transparent px-3 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="pr-3.5 text-slate-400 hover:text-slate-600 focus:outline-none"
                        aria-label={showPassword ? 'Ocultar contraseña' : 'Ver contraseña'}
                      >
                        <span className="material-symbols-outlined text-[19px]">
                          {showPassword ? 'visibility_off' : 'visibility'}
                        </span>
                      </button>
                    </div>
                  </div>

                  {/* Recordarme y Estado Seguro */}
                  <div className="flex items-center justify-between text-xs text-slate-600 pt-0.5">
                    <label className="flex items-center gap-2 cursor-pointer select-none">
                      <input
                        type="checkbox"
                        checked={rememberMe}
                        onChange={(e) => setRememberMe(e.target.checked)}
                        className="w-4 h-4 rounded border-slate-300 text-[#006591] focus:ring-[#006591]"
                      />
                      <span className="font-medium text-slate-600">Recordarme</span>
                    </label>
                    <span className="text-[11px] text-slate-400 flex items-center gap-1">
                      <span className="material-symbols-outlined text-[14px] text-emerald-600">
                        verified_user
                      </span>
                      Conexión segura SSL
                    </span>
                  </div>

                  {/* Botón Principal de Iniciar Sesión */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full mt-2 bg-[#0c1a2e] hover:bg-[#162a46] active:scale-[0.99] text-white font-bold py-3 px-4 rounded-2xl shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 text-sm disabled:opacity-60 min-h-[46px]"
                  >
                    {loading ? (
                      'Iniciando sesión...'
                    ) : (
                      <>
                        <span>Iniciar Sesión</span>
                        <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                          arrow_forward
                        </span>
                      </>
                    )}
                  </button>

                  {/* Alternador móvil */}
                  <div className="md:hidden text-center pt-2">
                    <button
                      type="button"
                      onClick={() => setIsRegistering(true)}
                      className="text-xs text-[#006591] hover:underline font-bold"
                    >
                      ¿Eres nuevo paciente? Regístrate aquí
                    </button>
                  </div>
                </form>
              </div>
            ) : (
              /* ---------------------------------------------------------- */
              /* VISTA 2: FORMULARIO DE REGISTRO DE PACIENTE                */
              /* ---------------------------------------------------------- */
              <div className="my-auto py-2 flex flex-col gap-3">
                <div>
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                    Crear Cuenta
                  </h1>
                  <p className="text-xs text-slate-500 font-medium mt-1">
                    Ingresa tus datos personales para tu registro como paciente.
                  </p>
                </div>

                <form onSubmit={handleRegistro} className="flex flex-col gap-2.5">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    {/* Nombres */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-nombres" className="text-[11px] font-bold text-slate-700">
                        Nombres *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          person
                        </span>
                        <input
                          id="reg-nombres"
                          type="text"
                          required
                          value={regForm.nombres}
                          onChange={(e) => setRegForm({ ...regForm, nombres: e.target.value })}
                          placeholder="Tus nombres"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Apellidos */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-apellidos" className="text-[11px] font-bold text-slate-700">
                        Apellidos *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          badge
                        </span>
                        <input
                          id="reg-apellidos"
                          type="text"
                          required
                          value={regForm.apellidos}
                          onChange={(e) => setRegForm({ ...regForm, apellidos: e.target.value })}
                          placeholder="Tus apellidos"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* DNI */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-dni" className="text-[11px] font-bold text-slate-700">
                        DNI (8 dígitos) *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          credit_card
                        </span>
                        <input
                          id="reg-dni"
                          type="text"
                          required
                          maxLength={8}
                          pattern="\d{8}"
                          value={regForm.dni}
                          onChange={(e) => setRegForm({ ...regForm, dni: e.target.value })}
                          placeholder="12345678"
                          className="w-full bg-transparent px-2.5 py-2 text-xs font-mono text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Fecha de Nacimiento */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-fnac" className="text-[11px] font-bold text-slate-700">
                        Fecha de Nacimiento *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          calendar_today
                        </span>
                        <input
                          id="reg-fnac"
                          type="date"
                          required
                          value={regForm.fechaNacimiento}
                          onChange={(e) =>
                            setRegForm({ ...regForm, fechaNacimiento: e.target.value })
                          }
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Teléfono */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-tel" className="text-[11px] font-bold text-slate-700">
                        Teléfono Móvil (9 dígitos) *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          call
                        </span>
                        <input
                          id="reg-tel"
                          type="tel"
                          required
                          pattern="\d{9}"
                          maxLength={9}
                          value={regForm.telefono}
                          onChange={(e) => setRegForm({ ...regForm, telefono: e.target.value })}
                          placeholder="987654321"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Correo Electrónico */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-correo" className="text-[11px] font-bold text-slate-700">
                        Correo Electrónico *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          mail
                        </span>
                        <input
                          id="reg-correo"
                          type="email"
                          required
                          value={regForm.correo}
                          onChange={(e) => setRegForm({ ...regForm, correo: e.target.value })}
                          placeholder="tu@correo.com"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Usuario */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-user" className="text-[11px] font-bold text-slate-700">
                        Nombre de Usuario *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          account_circle
                        </span>
                        <input
                          id="reg-user"
                          type="text"
                          required
                          value={regForm.username}
                          onChange={(e) => setRegForm({ ...regForm, username: e.target.value })}
                          placeholder="usuario123"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>

                    {/* Contraseña */}
                    <div className="flex flex-col gap-0.5">
                      <label htmlFor="reg-pass" className="text-[11px] font-bold text-slate-700">
                        Contraseña (mín. 6 caract.) *
                      </label>
                      <div className="relative flex items-center bg-slate-100/90 border border-slate-200 rounded-2xl focus-within:bg-white focus-within:border-[#006591] focus-within:ring-2 focus-within:ring-[#006591]/20 transition-all">
                        <span
                          className="material-symbols-outlined text-slate-400 text-[18px] pl-3 pointer-events-none select-none"
                          aria-hidden="true"
                        >
                          lock
                        </span>
                        <input
                          id="reg-pass"
                          type="password"
                          required
                          minLength={6}
                          value={regForm.password}
                          onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
                          placeholder="••••••••"
                          className="w-full bg-transparent px-2.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 outline-none focus:outline-none focus:ring-0"
                        />
                      </div>
                    </div>
                  </div>

                  {/* Botón Principal de Registro */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full mt-2 bg-[#0c1a2e] hover:bg-[#162a46] active:scale-[0.99] text-white font-bold py-2.5 px-4 rounded-2xl shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 text-xs disabled:opacity-60 min-h-[44px]"
                  >
                    {loading ? (
                      'Completando registro...'
                    ) : (
                      <>
                        <span>Registrarme como Paciente</span>
                        <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                          check_circle
                        </span>
                      </>
                    )}
                  </button>

                  {/* Alternador móvil */}
                  <div className="md:hidden text-center pt-1">
                    <button
                      type="button"
                      onClick={() => setIsRegistering(false)}
                      className="text-xs text-[#006591] hover:underline font-bold"
                    >
                      ¿Ya tienes cuenta? Inicia sesión aquí
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Pie de Página de Soporte */}
            <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
              <span>© 2026 CliniTech Healthcare</span>
              <span>v2.4 Pro</span>
            </div>
          </div>

          {/* ============================================================== */}
          {/* PANEL BANNER OSCURO (Derecha en Login, Izquierda en Registro) */}
          {/* ============================================================== */}
          <div className="w-full md:w-1/2 h-full rounded-[22px] sm:rounded-[28px] bg-gradient-to-br from-[#0c1a2e] via-[#091527] to-[#040913] text-white p-8 sm:p-10 lg:p-12 flex flex-col items-center justify-center text-center relative overflow-hidden min-h-[360px] md:min-h-0">
            {/* Marca de agua vectorial decorativa en el fondo */}
            <div
              className="absolute inset-0 flex items-center justify-center pointer-events-none select-none overflow-hidden"
              aria-hidden="true"
            >
              <svg
                viewBox="0 0 200 200"
                className="w-[120%] h-[120%] text-white/[0.04] transform -rotate-12"
                fill="currentColor"
              >
                <path d="M85,15 h30 v55 h55 v30 h-55 v55 h-30 v-55 h-55 v-30 h55 z" />
              </svg>
            </div>

            {/* Resplandor ambiental interno */}
            <div
              className="absolute -top-24 -right-24 w-72 h-72 rounded-full bg-sky-500/10 blur-3xl pointer-events-none"
              aria-hidden="true"
            />
            <div
              className="absolute -bottom-24 -left-24 w-72 h-72 rounded-full bg-blue-600/10 blur-3xl pointer-events-none"
              aria-hidden="true"
            />

            {/* Contenido Dinámico del Banner Oscuro */}
            <div className="relative z-10 max-w-sm flex flex-col items-center">
              {!isRegistering ? (
                <>
                  <div className="w-14 h-14 rounded-2xl bg-white/10 border border-white/20 flex items-center justify-center mb-6 shadow-inner backdrop-blur-md">
                    <span
                      className="material-symbols-outlined text-[30px] text-sky-300"
                      aria-hidden="true"
                    >
                      person_add
                    </span>
                  </div>

                  <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight mb-4">
                    Comenzar
                  </h2>

                  <p className="text-sm text-slate-300 leading-relaxed mb-8">
                    ¿Eres un nuevo paciente? Regístrate en nuestra plataforma para agendar tus
                    consultas médicas, consultar recetas y acceder a tu historial clínico en línea.
                  </p>

                  <button
                    type="button"
                    onClick={() => {
                      setError('');
                      setIsRegistering(true);
                    }}
                    className="px-9 py-3.5 rounded-full border-2 border-white/80 hover:border-white text-white font-bold text-xs uppercase tracking-widest hover:bg-white hover:text-slate-900 active:scale-95 transition-all duration-200 shadow-lg backdrop-blur-xs"
                  >
                    Registrarse
                  </button>
                </>
              ) : (
                <>
                  <div className="w-14 h-14 rounded-2xl bg-white/10 border border-white/20 flex items-center justify-center mb-6 shadow-inner backdrop-blur-md">
                    <span
                      className="material-symbols-outlined text-[30px] text-sky-300"
                      aria-hidden="true"
                    >
                      login
                    </span>
                  </div>

                  <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight mb-4">
                    ¡Bienvenido de nuevo!
                  </h2>

                  <p className="text-sm text-slate-300 leading-relaxed mb-8">
                    Si ya formas parte del equipo médico, personal asistencial o ya eres paciente
                    registrado, inicia sesión para continuar con tu atención.
                  </p>

                  <button
                    type="button"
                    onClick={() => {
                      setError('');
                      setIsRegistering(false);
                    }}
                    className="px-9 py-3.5 rounded-full border-2 border-white/80 hover:border-white text-white font-bold text-xs uppercase tracking-widest hover:bg-white hover:text-slate-900 active:scale-95 transition-all duration-200 shadow-lg backdrop-blur-xs"
                  >
                    Iniciar Sesión
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}