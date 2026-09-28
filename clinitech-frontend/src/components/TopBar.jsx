// src/components/TopBar.jsx
export default function TopBar() {
  return (
    <header className="fixed top-0 right-0 w-full md:w-[calc(100%-260px)] z-40 bg-[#f6faff] h-16 px-6 flex justify-between items-center">
      {/* Espacio vacío para balance */}
      <div className="hidden md:block flex-1"></div>

      {/* Acciones derecha */}
      <div className="flex items-center gap-4">
        {/* Buscador */}
        <div className="relative hidden sm:block">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[#6e7881] text-[20px]">
            search
          </span>
          <input
            type="text"
            placeholder="Buscar"
            className="pl-10 pr-4 py-2 bg-[#f0f4fa] rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-[#006591]/20 w-64"
          />
        </div>

        {/* Iconos */}
        <button className="text-[#6e7881] hover:text-[#006591] transition-colors">
          <span className="material-symbols-outlined">notifications</span>
        </button>
        <button className="text-[#6e7881] hover:text-[#006591] transition-colors">
          <span className="material-symbols-outlined">settings</span>
        </button>

        {/* Avatar */}
        <div className="w-8 h-8 rounded-full bg-[#0ea5e9] overflow-hidden cursor-pointer flex items-center justify-center text-white text-xs font-bold">
          SA
        </div>
      </div>
    </header>
  );
}