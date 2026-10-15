'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useSidebar } from '@/context/SidebarContext';
import { NAV_ITEMS } from '@/constants/navigation';
import { Icon } from '@/components/ui/Icon';

export interface SidebarProps {
  className?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ className = '' }) => {
  const { role } = useAuth();
  const { isOpen, close } = useSidebar();
  const pathname = usePathname();

  // Filtrar items por rol activo
  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <>
      {/* Backdrop overlay para pantallas pequeñas o drawer abierto */}
      {isOpen && (
        <div
          onClick={close}
          className="fixed inset-0 bg-black/40 z-30 transition-opacity backdrop-blur-xs"
        />
      )}

      {/* Drawer lateral */}
      <aside
        className={`fixed top-0 left-0 h-full w-72 bg-[#2E2E2E] text-white z-40 transform transition-transform duration-300 ease-in-out shadow-2xl flex flex-col ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        } ${className}`}
      >
        {/* Cabecera del Sidebar */}
        <div className="h-20 flex items-center justify-between px-6 bg-[#252525] border-b border-white/10">
          <div className="flex items-center gap-3">
            <Icon name="groups" size={26} className="text-[#CDCEE6]" />
            <span className="font-semibold text-lg tracking-wide text-white">Menú Principal</span>
          </div>
          <button
            type="button"
            onClick={close}
            className="p-1.5 rounded-lg hover:bg-white/10 text-white/80 hover:text-white cursor-pointer"
            aria-label="Cerrar menú lateral"
          >
            <Icon name="close" size={20} />
          </button>
        </div>

        {/* Lista de navegación */}
        <nav className="flex-1 overflow-y-auto py-6 px-4 space-y-1.5">
          <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-[#9E9E9E]">
            {role === 'CONSULTOR' ? 'Gestión Consultor' : 'Área Candidato'}
          </div>

          {visibleItems.map((item) => {
            const isActive = pathname === item.href || (item.href !== '/' && pathname.startsWith(item.href));

            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={close}
                className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-base font-medium transition-all select-none ${
                  isActive
                    ? 'bg-[#3F2581] text-white shadow-md font-semibold'
                    : 'text-[#E0E0E0] hover:bg-white/10 hover:text-white'
                }`}
              >
                <Icon
                  name={item.icon}
                  size={22}
                  className={isActive ? 'text-white' : 'text-[#CDCEE6]'}
                />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* Pie del Sidebar */}
        <div className="p-4 bg-[#252525] border-t border-white/10 text-xs text-[#9E9E9E] flex items-center justify-between">
          <span>Consultora Capit@l Humano</span>
          <span>v1.0.0</span>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
