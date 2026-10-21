import { useEffect, useState } from 'react';
import { api } from '../api';

export default function Reports() {
  const hoy = new Date().toISOString().split('T')[0];
  const haceUnMes = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const [desde, setDesde] = useState(haceUnMes);
  const [hasta, setHasta] = useState(hoy);
  const [reporte, setReporte] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const cargarReporte = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await api(`/api/reportes/administrativo?desde=${desde}&hasta=${hasta}`);
      setReporte(data);
    } catch (err) {
      setError(err.message || 'Error al compilar el reporte estadístico.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarReporte();
  }, []);

  const maxCitas = reporte?.medicosMasSolicitados?.length
    ? Math.max(...reporte.medicosMasSolicitados.map((m) => m.citas || 1))
    : 1;

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Encabezado y Selector de Fechas */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Informes y Estadísticas Clínicas
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            Indicadores asistenciales, métricas de atención hospitalaria y ranking de demanda médica.
          </p>
        </div>

        {/* Filtro de Rango Accesible */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            cargarReporte();
          }}
          className="flex flex-row items-center gap-2 sm:gap-3 bg-white p-2.5 rounded-2xl border border-slate-200 shadow-xs flex-wrap sm:flex-nowrap self-start"
        >
          <div className="flex items-center gap-1.5">
            <label htmlFor="rep-desde" className="text-xs font-bold text-slate-700 whitespace-nowrap">
              Desde:
            </label>
            <input
              id="rep-desde"
              type="date"
              value={desde}
              onChange={(e) => setDesde(e.target.value)}
              className="text-xs px-2.5 py-2 border border-slate-300 rounded-xl bg-white text-slate-800 font-medium focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[40px] w-32 sm:w-36"
            />
          </div>

          <div className="flex items-center gap-1.5">
            <label htmlFor="rep-hasta" className="text-xs font-bold text-slate-700 whitespace-nowrap">
              Hasta:
            </label>
            <input
              id="rep-hasta"
              type="date"
              value={hasta}
              onChange={(e) => setHasta(e.target.value)}
              className="text-xs px-2.5 py-2 border border-slate-300 rounded-xl bg-white text-slate-800 font-medium focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none min-h-[40px] w-32 sm:w-36"
            />
          </div>

          <button
            type="submit"
            className="shrink-0 px-4 py-2 bg-[#006591] hover:bg-[#004d70] text-white rounded-xl text-xs font-bold transition-all shadow-xs min-h-[40px] flex items-center gap-1.5 whitespace-nowrap"
          >
            <span className="material-symbols-outlined text-[18px]">filter_alt</span>
            Filtrar
          </button>
        </form>
      </div>

      {error && (
        <div role="alert" aria-live="polite" className="p-4 bg-rose-50 border-l-4 border-rose-600 rounded-r-xl flex items-start gap-3 shadow-xs">
          <span className="material-symbols-outlined text-rose-600 text-[20px] shrink-0 mt-0.5" aria-hidden="true">
            error
          </span>
          <div className="text-xs text-rose-800 font-medium">{error}</div>
        </div>
      )}

      {/* Tarjetas Resumen de Indicadores Clave */}
      {reporte && (
        <section aria-label="Tarjetas de indicadores de gestión" className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          <div className="bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
            <span className="text-xs uppercase font-extrabold text-slate-500 tracking-wider">Total Citas</span>
            <p className="text-3xl font-extrabold text-slate-900 mt-2">{reporte.totalCitas}</p>
            <span className="text-[11px] text-slate-500 mt-1 block">En el período consultado</span>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
            <span className="text-xs uppercase font-extrabold text-emerald-700 tracking-wider">Atendidas</span>
            <p className="text-3xl font-extrabold text-emerald-700 mt-2">{reporte.citasAtendidas}</p>
            <span className="text-[11px] text-emerald-600 font-medium mt-1 block">Completadas con éxito</span>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
            <span className="text-xs uppercase font-extrabold text-rose-700 tracking-wider">Canceladas</span>
            <p className="text-3xl font-extrabold text-rose-700 mt-2">{reporte.citasCanceladas}</p>
            <span className="text-[11px] text-rose-600 font-medium mt-1 block">Citas suspendidas</span>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
            <span className="text-xs uppercase font-extrabold text-[#006591] tracking-wider">Médicos Activos</span>
            <p className="text-3xl font-extrabold text-[#006591] mt-2">{reporte.medicosActivos}</p>
            <span className="text-[11px] text-sky-700 font-medium mt-1 block">Con atenciones brindadas</span>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs hover:shadow-md transition-shadow">
            <span className="text-xs uppercase font-extrabold text-[#006591] tracking-wider">Pacientes Atendidos</span>
            <p className="text-3xl font-extrabold text-[#006591] mt-2">{reporte.pacientesActivos}</p>
            <span className="text-[11px] text-sky-700 font-medium mt-1 block">Usuarios únicos</span>
          </div>
        </section>
      )}

      {/* Ranking Accesible de Médicos Más Solicitados */}
      <section aria-labelledby="ranking-medicos-heading" className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-200">
          <h2 id="ranking-medicos-heading" className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <span className="material-symbols-outlined text-[#006591]">leaderboard</span>
            Ranking de Médicos con Mayor Número de Citas
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Distribución proporcional de volumen de atenciones médicas en el centro de salud.
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <caption className="sr-only">Ranking de médicos con mayor volumen de citas</caption>
            <thead className="bg-slate-100/80 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200">
              <tr>
                <th scope="col" className="py-3 px-4 w-24">Posición</th>
                <th scope="col" className="py-3 px-4">Médico</th>
                <th scope="col" className="py-3 px-4">Demanda Proporcional</th>
                <th scope="col" className="py-3 px-4 text-right">Total Citas</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loading ? (
                <tr>
                  <td colSpan="4" className="py-8 text-center text-xs text-slate-500">
                    Cargando reporte administrativo...
                  </td>
                </tr>
              ) : !reporte?.medicosMasSolicitados || reporte.medicosMasSolicitados.length === 0 ? (
                <tr>
                  <td colSpan="4" className="py-10 text-center text-xs text-slate-500">
                    No se registran citas en este rango de fechas.
                  </td>
                </tr>
              ) : (
                reporte.medicosMasSolicitados.map((m, index) => {
                  const percentage = Math.round((m.citas / maxCitas) * 100);
                  return (
                    <tr key={m.medicoId} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3.5 px-4">
                        {index === 0 ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-amber-100 text-amber-900 border border-amber-300 font-extrabold text-xs rounded-full">
                            🥇 #1
                          </span>
                        ) : index === 1 ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-slate-200 text-slate-800 border border-slate-300 font-extrabold text-xs rounded-full">
                            🥈 #2
                          </span>
                        ) : index === 2 ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-orange-100 text-orange-900 border border-orange-300 font-extrabold text-xs rounded-full">
                            🥉 #3
                          </span>
                        ) : (
                          <span className="text-xs font-bold text-slate-500 pl-2">#{index + 1}</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 font-bold text-slate-900">{m.nombre}</td>
                      <td className="py-3.5 px-4">
                        <div className="w-full max-w-xs bg-slate-100 rounded-full h-2.5 overflow-hidden">
                          <div
                            className="bg-[#006591] h-2.5 rounded-full transition-all duration-500"
                            style={{ width: `${percentage}%` }}
                            aria-valuenow={percentage}
                            aria-valuemin="0"
                            aria-valuemax="100"
                            role="progressbar"
                          />
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-right font-mono font-bold text-slate-900 text-sm">
                        {m.citas}
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
  );
}