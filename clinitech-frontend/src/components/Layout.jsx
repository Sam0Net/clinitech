// src/components/Layout.jsx
import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

export default function Layout() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans">
      {/* Accesibilidad: Enlace de salto al contenido principal (WCAG 2.4.1) */}
      <a href="#main-content" className="skip-to-content">
        Saltar al contenido principal
      </a>

      {/* Sidebar navegable (escritorio fijo + móvil deslizable) */}
      <Sidebar
        mobileOpen={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
      />

      {/* Área de contenido */}
      <div className="md:pl-[260px] flex flex-col flex-1 min-h-screen">
        <TopBar onOpenMobileMenu={() => setMobileMenuOpen(true)} />

        <main
          id="main-content"
          tabIndex="-1"
          className="pt-20 px-4 sm:px-6 lg:px-8 pb-12 max-w-[1440px] w-full mx-auto flex-1 focus:outline-none"
        >
          <Outlet />
        </main>

        <footer className="py-4 px-6 border-t border-slate-200 text-center text-xs text-slate-500">
          <p>© {new Date().getFullYear()} CliniTech — Plataforma de Gestión Médica y Asistencial. Cumplimiento WCAG 2.2 AA.</p>
        </footer>
      </div>
    </div>
  );
}