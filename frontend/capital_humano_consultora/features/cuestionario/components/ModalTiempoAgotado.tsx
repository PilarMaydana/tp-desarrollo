'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalTiempoAgotadoProps {
  isOpen: boolean;
  onAceptar: () => void;
}

/**
 * Modal informativo cuando el contador local del cuestionario llega a 00:00.
 * Al cerrarlo, lleva a la pantalla de finalización por tiempo agotado sin llamadas extra al backend.
 */
export const ModalTiempoAgotado: React.FC<ModalTiempoAgotadoProps> = ({
  isOpen,
  onAceptar,
}) => {
  const aceptarBtnRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      const timer = window.setTimeout(() => {
        aceptarBtnRef.current?.focus();
      }, 50);
      return () => window.clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-tiempo-agotado"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
    >
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-amber-300 p-6 sm:p-8 flex flex-col items-center text-center space-y-5">
        <div className="w-14 h-14 rounded-full bg-amber-50 text-amber-700 flex items-center justify-center">
          <Icon name="timer" size={32} />
        </div>

        <div className="space-y-2">
          <h2
            id="modal-titulo-tiempo-agotado"
            className="text-xl font-bold text-[#3F2581]"
          >
            Se agotó el tiempo máximo para completar el cuestionario
          </h2>
          <p className="text-sm sm:text-base text-[#4A4A4A] leading-relaxed">
            La edición de respuestas ha sido bloqueada porque finalizó el tiempo disponible para completar la evaluación.
          </p>
        </div>

        <div className="w-full pt-2">
          <Button
            ref={aceptarBtnRef}
            type="button"
            variant="primary"
            onClick={onAceptar}
            className="w-full font-semibold focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalTiempoAgotado;
