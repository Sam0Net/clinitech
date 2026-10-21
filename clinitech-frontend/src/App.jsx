import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Appointments from './pages/Appointments';
import ClinicalHistory from './pages/ClinicalHistory';
import DoctorsManagement from './pages/DoctorsManagement';
import UsersManagement from './pages/UsersManagement';
import Reports from './pages/Reports';

function ProtectedRoute() {
  const token = localStorage.getItem('clinitech_token');
  return token ? <Layout /> : <Navigate to="/login" replace />;
}

// Redirección inteligente de inicio según el perfil del usuario autenticado
function DefaultHome() {
  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  if (user.role === 'PACIENTE') {
    return <Navigate to="/appointments" replace />;
  }
  return <Navigate to="/dashboard" replace />;
}

// Guardia de rutas por rol: si no tiene permiso, lo redirige al módulo correspondiente
function RoleRoute({ allowedRoles, children }) {
  const user = JSON.parse(localStorage.getItem('clinitech_user') || '{}');
  const role = user.role || '';
  if (!allowedRoles.includes(role)) {
    const fallback = role === 'PACIENTE' ? '/appointments' : '/dashboard';
    return <Navigate to={fallback} replace />;
  }
  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<DefaultHome />} />
          <Route
            path="/dashboard"
            element={
              <RoleRoute allowedRoles={['ADMIN', 'MEDICO', 'RECEPCIONISTA']}>
                <Dashboard />
              </RoleRoute>
            }
          />
          <Route path="/appointments" element={<Appointments />} />
          <Route path="/history" element={<ClinicalHistory />} />
          <Route
            path="/doctors"
            element={
              <RoleRoute allowedRoles={['ADMIN']}>
                <DoctorsManagement />
              </RoleRoute>
            }
          />
          <Route
            path="/users"
            element={
              <RoleRoute allowedRoles={['ADMIN']}>
                <UsersManagement />
              </RoleRoute>
            }
          />
          <Route
            path="/reports"
            element={
              <RoleRoute allowedRoles={['ADMIN']}>
                <Reports />
              </RoleRoute>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}