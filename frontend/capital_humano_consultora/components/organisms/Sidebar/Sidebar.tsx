'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useSidebar } from '@/context/SidebarContext';
import { NAV_ITEMS } from '@/constants/navigation';
import { Icon } from '@/components/ui/Icon';

export interface SidebarProps {
  className?: string;
}

function getRoutePrefix(href: string): string {
  const cleanPath = href.split('?')[0];
  const firstSegment = cleanPath.split('/').filter(Boolean)[0];
  return firstSegment ? `/${firstSegment}` : '/';
}

function isRouteActive(href: string, pathname: string): boolean {
  if (!pathname) return false;
  if (href.startsWith('/en-desarrollo')) {
    return pathname === href;
  }
  const prefix = getRoutePrefix(href);
  if (prefix === '/puestos' && pathname === '/') {
    return true;
  }
  if (prefix === '/') {
    return pathname === '/';
  }
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

export const Sidebar: React.FC<SidebarProps> = ({ className = '' }) => {
  const { role } = useAuth();
  const { isOpen, close } = useSidebar();
  const pathname = usePathname();

  const [manualToggle, setManualToggle] = useState<{
    pathname: string;
    groups: Record<string, boolean>;
  }>({ pathname, groups: {} });

  // Filtrar items por rol activo
  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  const handleToggleGroup = (label: string, currentExpanded: boolean) => {
    setManualToggle((prev) => ({
      pathname,
      groups: {
        ...(prev.pathname === pathname ? prev.groups : {}),
        [label]: !currentExpanded,
      },
    }));
  };

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
            if (item.type === 'group') {
              const isGroupRoute = item.children.some((child) =>
                isRouteActive(child.href, pathname)
              );
              const isExpanded =
                manualToggle.pathname === pathname && item.label in manualToggle.groups
                  ? manualToggle.groups[item.label]
                  : isGroupRoute;

              return (
                <div
                  key={item.label}
                  className={`rounded-xl border transition-colors bg-[#2E2E2E] ${
                    isExpanded
                      ? 'border-white/10'
                      : 'border-transparent'
                  }`}
                >
                  <button
                    type="button"
                    aria-expanded={isExpanded}
                    onClick={() => handleToggleGroup(item.label, isExpanded)}
                    className="w-full flex items-center justify-between gap-2 px-4 py-3 rounded-xl text-base font-medium transition-all select-none cursor-pointer text-[#E0E0E0] hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/20"
                  >
                    <span className="flex items-center gap-3.5 min-w-0">
                      <Icon
                        name={item.icon}
                        size={22}
                        className="text-[#CDCEE6] shrink-0"
                      />
                      <span className="whitespace-nowrap text-left">{item.label}</span>
                    </span>
                    <Icon
                      name={isExpanded ? 'chevron_down' : 'chevron_right'}
                      size={18}
                      className="text-[#CDCEE6] shrink-0"
                    />
                  </button>

                  {isExpanded && (
                    <div className="px-2 pb-2 pt-1 space-y-1">
                      {item.children.map((subItem) => {
                        if (!subItem.enabled) {
                          return (
                            <div
                              key={subItem.href}
                              aria-disabled="true"
                              title="Próximamente"
                              className="flex items-center gap-3 pl-6 pr-3 py-2 rounded-lg text-base font-medium text-[#9E9E9E]/60 cursor-not-allowed select-none"
                            >
                              {subItem.icon && (
                                <Icon
                                  name={subItem.icon}
                                  size={18}
                                  className="text-[#9E9E9E]/60 shrink-0"
                                />
                              )}
                              <span>{subItem.label}</span>
                            </div>
                          );
                        }

                        return (
                          <Link
                            key={subItem.href}
                            href={subItem.href}
                            onClick={close}
                            className="flex items-center gap-3 pl-6 pr-3 py-2 rounded-lg text-base font-medium transition-all select-none text-[#E0E0E0] hover:bg-white/10 hover:text-white"
                          >
                            {subItem.icon && (
                              <Icon
                                name={subItem.icon}
                                size={18}
                                className="text-[#CDCEE6] shrink-0"
                              />
                            )}
                            <span>{subItem.label}</span>
                          </Link>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            }

            if (!item.enabled) {
              return (
                <div
                  key={item.href}
                  aria-disabled="true"
                  title="Próximamente"
                  className="flex items-center gap-3.5 px-4 py-3 rounded-xl text-base font-medium text-[#9E9E9E]/60 cursor-not-allowed select-none"
                >
                  <Icon
                    name={item.icon}
                    size={22}
                    className="text-[#9E9E9E]/60"
                  />
                  <span>{item.label}</span>
                </div>
              );
            }

            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={close}
                className="flex items-center gap-3.5 px-4 py-3 rounded-xl text-base font-medium transition-all select-none text-[#E0E0E0] hover:bg-white/10 hover:text-white"
              >
                <Icon
                  name={item.icon}
                  size={22}
                  className="text-[#CDCEE6]"
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
