// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Appointments from './pages/Appointments';
import ClinicalHistory from './pages/ClinicalHistory';
import DoctorsManagement from './pages/DoctorsManagement';
import Reports from './pages/Reports';

function App() {
  // Simulación de autenticación (luego conectas con Spring Boot)
  const isAuthenticated = true; // Cambia a true para probar el dashboard

  return (
    <BrowserRouter>
      <Routes>
        {/* Si NO está autenticado, redirige todo a /login */}
        <Route path="/login" element={<Login />} />
        
        {/* Rutas protegidas */}
        <Route element={isAuthenticated ? <Layout /> : <Navigate to="/login" replace />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/appointments" element={<Appointments />} />
          <Route path="/history" element={<ClinicalHistory />} />
          <Route path="/doctors" element={<DoctorsManagement />} />
          <Route path="/reports" element={<Reports />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;