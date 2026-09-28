// src/pages/Login.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  const navigate = useNavigate();
  const [role, setRole] = useState('doctor');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Aquí luego harás la petición a Spring Boot:
    // axios.post('http://localhost:8080/api/auth/login', { username, password, role })
    
    // Por ahora, simulamos login exitoso:
    console.log('Login:', { username, password, role });
    
    // Redirige al dashboard
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f6faff] relative overflow-hidden p-4">
      {/* Fondo decorativo */}
      <div className="absolute inset-0 pointer-events-none">
        <div className="absolute top-[-20%] right-[-10%] w-[60vw] h-[60vw] rounded-full bg-[#c9e6ff]/30 blur-[120px]" />
        <div className="absolute bottom-[-20%] left-[-10%] w-[50vw] h-[50vw] rounded-full bg-[#cce5ff]/30 blur-[100px]" />
      </div>

      {/* Card */}
      <div className="w-full max-w-[440px] bg-white rounded-xl shadow-[0_4px_24px_-4px_rgba(0,0,0,0.08)] relative z-10">
        <div className="h-1 w-full bg-[#006591]" />
        
        <div className="p-8 flex flex-col gap-8">
          {/* Branding */}
          <div className="flex flex-col items-center text-center gap-3">
            <div className="w-14 h-14 rounded-full bg-[#c9e6ff] flex items-center justify-center text-[#006591]">
              <span className="material-symbols-outlined text-[32px]">medical_services</span>
            </div>
            <div>
              <h1 className="text-4xl font-bold text-[#006591] tracking-tight">CliniTech</h1>
              <p className="text-sm text-[#3e4850] mt-1">Gestión Médica</p>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-6">
            {/* Role Selector */}
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-[#3e4850]">
                Tipo de Cuenta
              </label>
              <div className="grid grid-cols-3 gap-1 bg-[#eaeef4] p-1 rounded-lg">
                {[
                  { value: 'patient', label: 'Paciente' },
                  { value: 'doctor', label: 'Médico' },
                  { value: 'admin', label: 'Admin' }
                ].map((r) => (
                  <label key={r.value} className="cursor-pointer text-center">
                    <input
                      type="radio"
                      name="role"
                      value={r.value}
                      checked={role === r.value}
                      onChange={() => setRole(r.value)}
                      className="peer sr-only"
                    />
                    <div className={`py-2 rounded-md text-xs font-semibold transition-all ${
                      role === r.value 
                        ? 'bg-white text-[#006591] shadow-sm' 
                        : 'text-[#3e4850]'
                    }`}>
                      {r.label}
                    </div>
                  </label>
                ))}
              </div>
            </div>

            {/* Inputs */}
            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold text-[#3e4850]">Usuario / ID</label>
                <div className="relative">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[#6e7881]">
                    person
                  </span>
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Ingresa tus credenciales"
                    className="w-full pl-10 pr-4 py-3 rounded-lg border border-[#bec8d2] bg-white text-sm focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none transition-all"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <div className="flex justify-between">
                  <label className="text-xs font-semibold text-[#3e4850]">Contraseña</label>
                  <a href="#" className="text-xs text-[#006591] hover:text-[#00476e]">¿Olvidaste tu contraseña?</a>
                </div>
                <div className="relative">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[#6e7881]">
                    lock
                  </span>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-4 py-3 rounded-lg border border-[#bec8d2] bg-white text-sm focus:border-[#006591] focus:ring-2 focus:ring-[#006591]/20 outline-none transition-all"
                  />
                </div>
              </div>
            </div>

            {/* Button */}
            <button
              type="submit"
              className="w-full bg-[#006591] text-white font-semibold py-3 rounded-lg hover:bg-[#00476e] active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-sm"
            >
              Acceder al Sistema
              <span className="material-symbols-outlined text-[20px]">arrow_forward</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}