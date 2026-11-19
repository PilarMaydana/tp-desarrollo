'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type { ErrorPanelState } from '../types';

export interface ModalExportarExcelProps {
  isOpen: boolean;
  isExporting: boolean;
  exportError: ErrorPanelState | null;
  onDismissExportError: () => void;
  onConfirmarSi: () => void;
  onRechazarNo: () => void;
}

/**
 * Modal de confirmación de creación y exportación a Excel (CU 025 - Pasos 13, 14 y 14.A).
 */
export const ModalExportarExcel: React.FC<ModalExportarExcelProps> = ({
  isOpen,
  isExporting,
  exportError,
  onDismissExportError,
  onConfirmarSi,
  onRechazarNo,
}) => {
  const siButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    const timer = window.setTimeout(() => {
      siButtonRef.current?.focus();
    }, 50);
    return () => window.clearTimeout(timer);
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-exportar-excel"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center gap-5">
        <div className="w-14 h-14 rounded-full bg-[#E8F5E9] text-[#1B8354] flex items-center justify-center shadow-inner">
          <Icon name="check_circle" size={32} />
        </div>

        <div className="space-y-2 w-full">
          <h2
            id="modal-titulo-exportar-excel"
            className="text-xl font-bold text-[#3F2581]"
          >
            Evaluación creada
          </h2>
          <p className="text-sm sm:text-base text-gray-800 leading-relaxed">
            La evaluación se ha creado correctamente. ¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?
          </p>
        </div>

        {exportError && (
          <div className="w-full text-left">
            <AlertaErrores
              titulo={exportError.titulo || 'Error al exportar el archivo Excel'}
              mensajeGeneral={exportError.mensajeGeneral}
              errores={exportError.detalles}
              onDismiss={onDismissExportError}
            />
          </div>
        )}

        <div className="grid grid-cols-2 gap-3 w-full pt-2">
          <Button
            type="button"
            variant="secondary"
            disabled={isExporting}
            onClick={onRechazarNo}
            className="w-full font-semibold"
          >
            No
          </Button>

          <Button
            ref={siButtonRef}
            type="button"
            variant="primary"
            isLoading={isExporting}
            disabled={isExporting}
            onClick={onConfirmarSi}
            className="w-full font-semibold"
          >
            Sí
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalExportarExcel;
