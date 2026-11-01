'use client';

import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { useSidebar } from '@/context/SidebarContext';
import { UserProfileBadge } from '@/components/molecules/UserProfileBadge';
import { Icon } from '@/components/ui/Icon';
import { useRouter } from 'next/navigation';
import { ROUTES } from '@/constants/routes';
import { User } from '@/types/auth';

export interface TopbarProps {
  consultorNombre?: string;
  className?: string;
}

const DEFAULT_MOCK_CONSULTOR: User = {
  id: 'usr-default-consultor',
  name: 'LIC. MARTÍN GÓMEZ',
  username: 'mgomez',
  email: 'martin.gomez@consultora.com',
  role: 'CONSULTOR',
};

export const Topbar: React.FC<TopbarProps> = ({ consultorNombre, className = '' }) => {
  const { user, logout } = useAuth();
  const { toggle } = useSidebar();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push(ROUTES.AUTH.LOGIN);
  };

  // Resolver usuario a mostrar: parámetro explícito > usuario autenticado > mockup por defecto
  const displayUser: User = consultorNombre
    ? {
        id: 'usr-custom',
        name: consultorNombre,
        username: consultorNombre.toLowerCase().replace(/\s+/g, '.'),
        email: `${consultorNombre.toLowerCase().replace(/\s+/g, '.')}@consultora.com`,
        role: 'CONSULTOR',
      }
    : user || DEFAULT_MOCK_CONSULTOR;

  return (
    <header
      className={`bg-[#3B3B3B] text-white h-20 px-4 md:px-8 lg:px-12 flex items-center justify-between shadow-md select-none sticky top-0 z-20 w-full ${className}`}
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

      {/* Sección Derecha: Usuario Consultor + Acciones */}
      <div className="flex items-center gap-3 md:gap-6">
        <UserProfileBadge user={displayUser} />

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
