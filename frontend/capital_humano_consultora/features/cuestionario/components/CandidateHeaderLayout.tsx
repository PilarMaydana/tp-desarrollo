'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';

export interface CandidateHeaderLayoutProps {
  children: React.ReactNode;
}

/**
 * Layout mínimo exclusivo para las pantallas del CANDIDATO (CU 026).
 * No incluye DashboardLayout, Sidebar ni menú del consultor.
 */
export const CandidateHeaderLayout: React.FC<CandidateHeaderLayoutProps> = ({
  children,
}) => {
  return (
    <div className="min-h-screen w-full flex flex-col bg-[#F6F6F6] text-[#000000]">
      <header className="bg-[#3B3B3B] text-white h-16 md:h-20 px-4 sm:px-8 flex items-center justify-between shadow-md select-none shrink-0">
        <div className="flex items-center gap-3">
          <div className="text-white flex items-center" aria-hidden="true">
            <Icon name="groups" size={30} />
          </div>
          <span className="font-semibold text-sm sm:text-base md:text-xl tracking-wider text-white">
            CONSULTORA CAPIT@L HUMANO
          </span>
        </div>

        <span className="hidden sm:inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 text-xs font-medium text-white/90">
          <Icon name="clipboard_check" size={15} />
          Evaluación en línea
        </span>
      </header>

      <main className="flex-1 w-full max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-10">
        {children}
      </main>
    </div>
  );
};

export default CandidateHeaderLayout;
