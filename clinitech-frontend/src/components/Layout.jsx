// src/components/Layout.jsx
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

export default function Layout() {
  return (
    <div className="min-h-screen bg-[#f6faff] font-['Inter'] text-[#171c20]">
      {/* Sidebar */}
      <Sidebar />
      
      {/* Contenido principal */}
      <div className="md:ml-[260px]">
        <TopBar />
        
        <main className="pt-16 p-6 max-w-[1440px] mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}