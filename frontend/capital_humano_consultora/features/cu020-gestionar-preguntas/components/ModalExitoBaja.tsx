'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalExitoBajaProps {
  isOpen: boolean;
  preguntaNombre: string;
  onAceptar: () => void;
}

/**
 * Modal de Éxito posterior a la baja (CU 023).
 * Mensaje exacto de especificación:
 * «Los datos de la pregunta {nombre_pregunta} han sido eliminados del sistema.»
 */
export const ModalExitoBaja: React.FC<ModalExitoBajaProps> = ({
  isOpen,
  preguntaNombre,
  onAceptar,
}) => {
  const aceptarBtnRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      const timer = setTimeout(() => {
        aceptarBtnRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-exito-baja"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center space-y-5">
        {/* Ícono de confirmación positiva */}
        <div className="w-14 h-14 rounded-full bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shadow-inner">
          <Icon name="check" size={32} />
        </div>

        {/* Título y Mensaje de éxito */}
        <div className="space-y-2">
          <h2 id="modal-titulo-exito-baja" className="text-xl font-bold text-[#3F2581]">
            Baja Confirmada
          </h2>
          <p className="text-base text-gray-800 leading-relaxed">
            Los datos de la pregunta{' '}
            <strong className="text-[#3F2581] font-semibold break-words">
              {preguntaNombre}
            </strong>{' '}
            han sido eliminados del sistema.
          </p>
        </div>

        {/* Botón ACEPTAR */}
        <div className="w-full pt-2">
          <Button
            ref={aceptarBtnRef}
            type="button"
            variant="primary"
            onClick={onAceptar}
            className="w-full text-base font-semibold"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalExitoBaja;

