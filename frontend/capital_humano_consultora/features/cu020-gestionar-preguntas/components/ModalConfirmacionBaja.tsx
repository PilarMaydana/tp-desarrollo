'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalConfirmacionBajaProps {
  isOpen: boolean;
  preguntaNombre: string;
  isSubmitting?: boolean;
  onConfirmar: () => void;
  onCancelar: () => void;
}

/**
 * Modal de Confirmación de Baja (CU 023 - Gestionar Preguntas / Baja).
 * Especificación:
 * - Mensaje: «Los datos de la pregunta {nombre_pregunta} serán eliminados del sistema».
 * - Botones: [ELIMINAR] y [CANCELAR].
 */
export const ModalConfirmacionBaja: React.FC<ModalConfirmacionBajaProps> = ({
  isOpen,
  preguntaNombre,
  isSubmitting = false,
  onConfirmar,
  onCancelar,
}) => {
  const cancelarButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      // Auto-enfocar botón cancelar por seguridad para prevenir eliminaciones accidentales
      const timer = setTimeout(() => {
        cancelarButtonRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-baja-pregunta"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center space-y-5">
        {/* Ícono de advertencia de eliminación */}
        <div className="w-14 h-14 rounded-full bg-red-100 text-[#D32F2F] flex items-center justify-center shadow-inner">
          <Icon name="trash" size={30} />
        </div>

        {/* Título y Mensaje formal CU 023 */}
        <div className="space-y-2">
          <h2 id="modal-titulo-baja-pregunta" className="text-xl font-bold text-[#3F2581]">
            Confirmar Eliminación
          </h2>
          <p className="text-base text-gray-800 leading-relaxed">
            Los datos de la pregunta{' '}
            <strong className="text-[#3F2581] font-semibold break-words">
              {preguntaNombre}
            </strong>{' '}
            serán eliminados del sistema.
          </p>
          <p className="text-sm font-medium text-[#757575]">
            Esta acción no se puede deshacer. ¿Desea proceder?
          </p>
        </div>

        {/* Botones CANCELAR y ELIMINAR */}
        <div className="grid grid-cols-2 gap-3 w-full pt-2">
          <Button
            ref={cancelarButtonRef}
            type="button"
            variant="secondary"
            onClick={onCancelar}
            disabled={isSubmitting}
            className="w-full text-base font-semibold"
          >
            CANCELAR
          </Button>

          <Button
            type="button"
            variant="danger"
            onClick={onConfirmar}
            isLoading={isSubmitting}
            className="w-full text-base font-semibold"
          >
            ELIMINAR
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalConfirmacionBaja;

