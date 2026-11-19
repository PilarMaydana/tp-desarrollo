'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalCompetenciasNoEvaluablesProps {
  isOpen: boolean;
  mensaje: string | null;
  competencias: string[];
  onAceptar: () => void;
}

/**
 * Modal del Flujo Alternativo 9.A (CU 025):
 * Muestra los nombres de las competencias que no pueden ser evaluadas.
 * Al presionar «Aceptar», finaliza el caso de uso y retorna a la pantalla principal.
 */
export const ModalCompetenciasNoEvaluables: React.FC<
  ModalCompetenciasNoEvaluablesProps
> = ({ isOpen, mensaje, competencias, onAceptar }) => {
  const aceptarButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    const timer = window.setTimeout(() => {
      aceptarButtonRef.current?.focus();
    }, 50);
    return () => window.clearTimeout(timer);
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-competencias-no-evaluables"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center gap-5">
        <div className="w-14 h-14 rounded-full bg-red-100 text-[#D32F2F] flex items-center justify-center shadow-inner">
          <Icon name="error" size={30} />
        </div>

        <div className="space-y-2 w-full">
          <h2
            id="modal-titulo-competencias-no-evaluables"
            className="text-xl font-bold text-[#3F2581]"
          >
            Competencias no evaluables
          </h2>

          {mensaje && (
            <p className="text-sm sm:text-base text-gray-800 leading-relaxed">
              {mensaje}
            </p>
          )}

          {competencias.length > 0 && (
            <div className="mt-3 text-left bg-red-50 border border-red-200 rounded-xl p-4">
              <p className="text-xs font-bold uppercase tracking-wider text-red-900 mb-2">
                Competencias afectadas:
              </p>
              <ul className="list-disc list-inside space-y-1 text-sm font-medium text-red-800">
                {competencias.map((nombre, idx) => (
                  <li key={`${nombre}-${idx}`}>{nombre}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="w-full pt-2 flex justify-center">
          <Button
            ref={aceptarButtonRef}
            type="button"
            variant="primary"
            onClick={onAceptar}
            className="min-w-[160px]"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalCompetenciasNoEvaluables;
