// src/pages/Dashboard.jsx
export default function Dashboard() {
  return (
    <div>
      <h1 className="text-2xl font-semibold text-[#171c20] mb-2">Citas de Hoy</h1>
      <p className="text-sm text-[#3e4850] mb-6">Administra tus citas programadas y tus casos críticos.</p>
      
      {/* Tarjetas de métricas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Tarjeta 1: Total de Citas */}
        <div className="bg-white rounded-xl p-6 shadow-[0_1px_3px_rgba(0,0,0,0.08)] hover:shadow-[0_4px_12px_rgba(0,0,0,0.1)] transition-shadow">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#6e7881]">Total de Citas</span>
            <div className="w-10 h-10 rounded-full bg-[#c9e6ff] flex items-center justify-center">
              <span className="material-symbols-outlined text-[#006591]">event_available</span>
            </div>
          </div>
          <p className="text-4xl font-bold text-[#171c20]">12</p>
        </div>

        {/* Tarjeta 2: Casos Urgentes */}
        <div className="bg-white rounded-xl p-6 shadow-[0_1px_3px_rgba(0,0,0,0.08)] hover:shadow-[0_4px_12px_rgba(0,0,0,0.1)] transition-shadow">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#ba1a1a]">Casos Urgentes</span>
            <div className="w-10 h-10 rounded-full bg-[#ffdad6] flex items-center justify-center">
              <span className="material-symbols-outlined text-[#ba1a1a]">emergency</span>
            </div>
          </div>
          <p className="text-4xl font-bold text-[#171c20]">3</p>
        </div>

        {/* Tarjeta 3: Completadas */}
        <div className="bg-white rounded-xl p-6 shadow-[0_1px_3px_rgba(0,0,0,0.08)] hover:shadow-[0_4px_12px_rgba(0,0,0,0.1)] transition-shadow">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#006c49]">Completadas</span>
            <div className="w-10 h-10 rounded-full bg-[#6ffbbe]/30 flex items-center justify-center">
              <span className="material-symbols-outlined text-[#006c49]">check_circle</span>
            </div>
          </div>
          <p className="text-4xl font-bold text-[#171c20]">4</p>
        </div>

      </div>
    </div>
  );
}