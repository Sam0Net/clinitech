// src/components/Sidebar.jsx
import { useEffect } from 'react';
import { NavLink } from 'react-router-dom';

const allMenuItems = [
  { icon: 'dashboard', label: 'Panel de control', path: '/dashboard', roles: ['ADMIN', 'MEDICO', 'RECEPCIONISTA'] },
  { icon: 'calendar_today', label: 'Citas', path: '/appointments', roles: ['ADMIN', 'MEDICO', 'PACIENTE', 'RECEPCIONISTA'] },
  { icon: 'history_edu', label: 'Historial Clínico', path: '/history', roles: ['ADMIN', 'MEDICO', 'PACIENTE', 'RECEPCIONISTA'] },
  { icon: 'medical_services', label: 'Gestión de Médicos', path: '/doctors', roles: ['ADMIN'] },
  { icon: 'manage_accounts', label: 'Gestión de Usuarios', path: '/users', roles: ['ADMIN'] },
  { icon: 'assessment', label: 'Informes', path: '/reports', roles: ['ADMIN'] },
];

export default function Sidebar({ mobileOpen = false, onClose = () => {} }) {
  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  const role = user.role || 'ADMIN';
  const menuItems = allMenuItems
    .filter((item) => item.roles.includes(role))
    .map((item) => {
      if (role === 'PACIENTE') {
        if (item.path === '/appointments') return { ...item, label: 'Mis Citas' };
        if (item.path === '/history') return { ...item, label: 'Mi Historial' };
      }
      return item;
    });

  // Cierre accesible con tecla Escape para usuarios de teclado
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && mobileOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mobileOpen, onClose]);

  const navContent = (
    <div className="flex flex-col h-full py-6 px-4">
      {/* Encabezado y Logo Clínico */}
      <div className="flex items-center justify-between px-2 mb-8">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#006591] to-[#0284c7] text-white flex items-center justify-center shadow-md">
            <span className="material-symbols-outlined text-[24px]">local_hospital</span>
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight text-slate-900 flex items-center gap-1">
              CliniTech
            </span>
            <span className="text-[11px] font-semibold text-[#006591] uppercase tracking-wider block">
              Gestión Clínica
            </span>
          </div>
        </div>

        {/* Botón de cierre en versión móvil */}
        <button
          onClick={onClose}
          aria-label="Cerrar menú de navegación"
          className="md:hidden p-2 text-slate-500 hover:text-slate-800 rounded-lg hover:bg-slate-200 transition-colors"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      {/* Lista de navegación accesible */}
      <nav aria-label="Menú principal de la clínica" className="flex-1 space-y-1.5">
        <div className="px-2 pb-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
          Módulos del Sistema
        </div>
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            onClick={() => onClose()}
            className={({ isActive }) =>
              `flex items-center gap-3.5 px-3.5 py-3 rounded-xl font-medium text-sm transition-all min-h-[44px] ${
                isActive
                  ? 'bg-[#006591] text-white shadow-sm font-semibold'
                  : 'text-slate-600 hover:bg-slate-200/60 hover:text-slate-900'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <span
                  className={`material-symbols-outlined text-[22px] ${
                    isActive ? 'text-white' : 'text-slate-500'
                  }`}
                >
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Tarjeta de usuario y botón de cerrar sesión */}
      <div className="pt-4 border-t border-slate-300/80 space-y-2">
        <div className="px-3 py-2.5 rounded-xl bg-white/70 border border-slate-200/80 flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-[#e0f2fe] text-[#006591] font-bold text-xs flex items-center justify-center shrink-0">
            {(user.username || 'U').substring(0, 2).toUpperCase()}
          </div>
          <div className="overflow-hidden">
            <p className="text-xs font-bold text-slate-800 truncate capitalize">
              {user.username || 'Usuario'}
            </p>
            <p className="text-[10px] text-slate-500 font-medium uppercase tracking-wider">
              {role}
            </p>
          </div>
        </div>

        <button
          onClick={() => {
            localStorage.removeItem('clinitech_token');
            localStorage.removeItem('clinitech_user');
            window.location.href = '/login';
          }}
          className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-rose-700 hover:bg-rose-50 transition-colors text-xs font-semibold min-h-[44px]"
          aria-label="Cerrar sesión en CliniTech"
        >
          <span className="material-symbols-outlined text-[20px] text-rose-600">logout</span>
          <span>Cerrar sesión</span>
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Sidebar fijo en Desktop */}
      <aside
        aria-label="Barra lateral de escritorio"
        className="fixed left-0 top-0 h-screen w-[260px] bg-slate-100 border-r border-slate-200 z-30 hidden md:block"
      >
        {navContent}
      </aside>

      {/* Sidebar móvil (Off-canvas drawer con backdrop) */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 md:hidden flex" role="dialog" aria-modal="true" aria-label="Menú móvil">
          {/* Backdrop con desenfoque suave */}
          <div
            className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity"
            onClick={onClose}
            aria-hidden="true"
          />

          {/* Panel deslizante */}
          <div className="relative w-[280px] max-w-[85vw] bg-slate-100 h-full shadow-2xl z-10 animate-in slide-in-from-left duration-200">
            {navContent}
          </div>
        </div>
      )}
    </>
  );
}