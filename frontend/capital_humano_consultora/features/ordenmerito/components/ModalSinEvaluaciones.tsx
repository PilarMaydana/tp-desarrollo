'use client';

import React, { useRef, type RefObject } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { useModalFocusTrap } from '../hooks/useModalFocusTrap';
import type { PuestoOrdenMeritoDTO } from '../types';

export interface ModalSinEvaluacionesProps {
  isOpen: boolean;
  puesto: PuestoOrdenMeritoDTO | null;
  mensaje: string | null;
  returnFocusRef?: RefObject<HTMLElement | null>;
  onAceptar: () => void;
}

/**
 * Modal informativo del Flujo Alternativo 6.A (CU 028):
 * Muestra el mensaje real del backend (HTTP 422) cuando el puesto no posee evaluaciones
 * y mantiene al actor en la grilla del paso 4 sin navegar.
 */
export const ModalSinEvaluaciones: React.FC<ModalSinEvaluacionesProps> = ({
  isOpen,
  puesto,
  mensaje,
  returnFocusRef,
  onAceptar,
}) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  const aceptarButtonRef = useRef<HTMLButtonElement>(null);

  useModalFocusTrap({
    isOpen,
    containerRef: dialogRef,
    initialFocusRef: aceptarButtonRef,
    returnFocusRef,
    onClose: onAceptar,
  });

  if (!isOpen || !mensaje) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-sin-evaluaciones-titulo"
      aria-describedby="modal-sin-evaluaciones-mensaje"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onAceptar();
        }
      }}
    >
      <div
        ref={dialogRef}
        tabIndex={-1}
        className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-7 flex flex-col items-center text-center gap-4"
      >
        <div className="w-14 h-14 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center shadow-inner">
          <Icon name="error" size={30} />
        </div>

        <div className="space-y-2 w-full">
          <h2
            id="modal-sin-evaluaciones-titulo"
            className="text-xl font-bold text-[#3F2581]"
          >
            Sin evaluaciones disponibles
          </h2>

          {puesto && (
            <p className="text-xs font-semibold text-[#757575]">
              {puesto.codigo} — {puesto.nombre} ({puesto.empresa})
            </p>
          )}

          <p
            id="modal-sin-evaluaciones-mensaje"
            className="text-sm sm:text-base text-[#1C1B1F] leading-relaxed pt-1"
          >
            {mensaje}
          </p>
        </div>

        <div className="w-full pt-2 flex justify-center">
          <Button
            ref={aceptarButtonRef}
            type="button"
            variant="primary"
            onClick={onAceptar}
            className="min-w-[140px]"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalSinEvaluaciones;
