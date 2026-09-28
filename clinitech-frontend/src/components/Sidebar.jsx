// src/components/Sidebar.jsx
import { NavLink } from 'react-router-dom';

const menuItems = [
  { icon: 'dashboard', label: 'Panel de control', path: '/dashboard' },
  { icon: 'calendar_today', label: 'Citas', path: '/appointments' },
  { icon: 'history_edu', label: 'Historial Clínico', path: '/history' },
  { icon: 'medical_services', label: 'Gestión de Médicos', path: '/doctors' },
  { icon: 'assessment', label: 'Informes', path: '/reports' },
];

export default function Sidebar() {
  return (
    <nav className="fixed left-0 top-0 h-screen w-[260px] bg-[#dee3e9] flex flex-col py-6 z-50 hidden md:flex">
      {/* Logo */}
      <div className="px-6 mb-8">
        <h1 className="text-xl font-bold text-[#006591]">CliniTech</h1>
        <p className="text-xs text-[#3e4850] uppercase tracking-wider">Gestión Médica</p>
      </div>

      {/* Navegación */}
      <div className="flex-1 px-2 space-y-1">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors duration-200 ${
                isActive
                  ? 'text-[#006591] font-bold border-r-4 border-[#006591] bg-[#c9e6ff]/30'
                  : 'text-[#3e4850] hover:bg-[#eaeef4] hover:text-[#171c20]'
              }`
            }
          >
            <span className="material-symbols-outlined">{item.icon}</span>
            <span className="text-sm">{item.label}</span>
          </NavLink>
        ))}
      </div>

      {/* Perfil y Logout */}
      <div className="px-2 mt-auto space-y-1">
        <NavLink
          to="/profile"
          className="flex items-center gap-3 px-4 py-3 rounded-lg text-[#3e4850] hover:bg-[#eaeef4] hover:text-[#171c20] transition-colors"
        >
          <span className="material-symbols-outlined">person</span>
          <span className="text-sm">Perfil</span>
        </NavLink>
        <NavLink
          to="/login"
          className="flex items-center gap-3 px-4 py-3 rounded-lg text-[#3e4850] hover:bg-[#eaeef4] hover:text-[#171c20] transition-colors"
        >
          <span className="material-symbols-outlined">logout</span>
          <span className="text-sm">Cerrar sesión</span>
        </NavLink>
      </div>
    </nav>
  );
}