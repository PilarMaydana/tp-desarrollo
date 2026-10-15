'use client';

import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { useSidebar } from '@/context/SidebarContext';
import { UserProfileBadge } from '@/components/molecules/UserProfileBadge';
import { Icon } from '@/components/ui/Icon';
import { useRouter } from 'next/navigation';
import { ROUTES } from '@/constants/routes';

export interface TopbarProps {
  className?: string;
}

export const Topbar: React.FC<TopbarProps> = ({ className = '' }) => {
  const { user, logout, role, switchRole } = useAuth();
  const { toggle } = useSidebar();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push(ROUTES.AUTH.LOGIN);
  };

  return (
    <header
      className={`bg-[#3B3B3B] text-white h-20 px-4 md:px-8 flex items-center justify-between rounded-t-xl shadow-md select-none ${className}`}
    >
      {/* Sección Izquierda: Menú Hamburguesa + Marca */}
      <div className="flex items-center gap-4 md:gap-6">
        <button
          type="button"
          onClick={toggle}
          className="p-2 rounded-lg hover:bg-white/10 active:bg-white/20 transition-colors text-white cursor-pointer"
          aria-label="Abrir o cerrar menú lateral"
        >
          <Icon name="menu" size={28} />
        </button>

        <div className="flex items-center gap-3">
          <div className="text-white hidden sm:flex items-center">
            <Icon name="groups" size={32} />
          </div>
          <span className="font-semibold text-base md:text-xl tracking-wider text-white">
            CONSULTORA CAPIT@L HUMANO
          </span>
        </div>
      </div>

      {/* Sección Derecha: Usuario + Acciones */}
      <div className="flex items-center gap-3 md:gap-6">
        {/* Switch rápido de rol para testing de interfaz */}
        <button
          type="button"
          onClick={() => {
            const nextRole = role === 'CONSULTOR' ? 'CANDIDATO' : 'CONSULTOR';
            switchRole(nextRole);
            if (nextRole === 'CANDIDATO') {
              router.push(ROUTES.CANDIDATO.CUESTIONARIO);
            } else {
              router.push(ROUTES.CONSULTOR.PUESTOS);
            }
          }}
          className="hidden lg:inline-flex items-center text-xs px-2.5 py-1 rounded-full bg-white/10 hover:bg-white/20 text-white/90 border border-white/20 transition-all cursor-pointer"
          title="Cambiar rol para probar interfaz"
        >
          Rol: <strong className="ml-1 text-white underline">{role}</strong> (Cambiar)
        </button>

        <UserProfileBadge user={user} />

        <div className="flex items-center gap-2">
          {/* Notificaciones */}
          <button
            type="button"
            className="p-2 rounded-lg bg-transparent hover:bg-white/10 text-white border border-white/20 transition-colors cursor-pointer relative"
            aria-label="Notificaciones"
            title="Notificaciones"
          >
            <Icon name="notifications" size={20} />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-[#D32F2F] rounded-full" />
          </button>

          {/* Salir */}
          <button
            type="button"
            onClick={handleLogout}
            className="p-2 rounded-lg bg-transparent hover:bg-white/10 text-white border border-white/20 transition-colors cursor-pointer"
            aria-label="Cerrar sesión"
            title="Cerrar sesión"
          >
            <Icon name="logout" size={20} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Topbar;
