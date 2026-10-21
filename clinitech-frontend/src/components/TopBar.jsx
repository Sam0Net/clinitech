// src/components/TopBar.jsx
import { useLocation } from 'react-router-dom';

const routeNames = {
  '/dashboard': 'Panel de Control y Triaje',
  '/appointments': 'Citas Médicas',
  '/history': 'Admisión y Ficha Clínica',
  '/doctors': 'Gestión Médica y Especialidades',
  '/reports': 'Informes y Estadísticas',
};

export default function TopBar({ onOpenMobileMenu = () => {} }) {
  const location = useLocation();
  const currentTitle = routeNames[location.pathname] || 'CliniTech';

  return (
    <header className="fixed top-0 right-0 w-full md:w-[calc(100%-260px)] z-20 bg-white/95 backdrop-blur-md h-16 px-4 sm:px-6 flex justify-between items-center border-b border-slate-200/80 shadow-xs">
      {/* Botón menú móvil + Título de sección activa */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          aria-label="Abrir menú de navegación"
          className="md:hidden w-11 h-11 flex items-center justify-center rounded-xl text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors focus-visible:ring-2 focus-visible:ring-[#006591]"
        >
          <span className="material-symbols-outlined text-[24px]">menu</span>
        </button>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400 hidden sm:inline">CliniTech /</span>
          <h2 className="text-sm sm:text-base font-bold text-slate-800 tracking-tight">
            {currentTitle}
          </h2>
        </div>
      </div>

      {/* Indicador de estado de conexión */}
      <div className="flex items-center">
        <div
          className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold shadow-2xs"
          title="Conexión en tiempo real con PostgreSQL y Spring Boot activa"
        >
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" aria-hidden="true" />
          <span>Clínica en línea</span>
        </div>
      </div>
    </header>
  );
}