'use client';

import React, { useRef, useState, type RefObject } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { useModalFocusTrap } from '../hooks/useModalFocusTrap';
import { formatFechaLocal } from '../utils/formatters';
import type { EvaluacionOrdenMeritoDTO, PuestoOrdenMeritoDTO } from '../types';

export interface ModalSeleccionarEvaluacionesProps {
  isOpen: boolean;
  puesto: PuestoOrdenMeritoDTO | null;
  evaluaciones: EvaluacionOrdenMeritoDTO[];
  returnFocusRef?: RefObject<HTMLElement | null>;
  onEmitir: (evaluacionIds?: number[]) => void;
  onCancelar: () => void;
}

const MENSAJE_SELECCION_VACIA = 'Seleccione una evaluación o TODAS';

interface ModalSeleccionarEvaluacionesContentProps {
  puesto: PuestoOrdenMeritoDTO;
  evaluaciones: EvaluacionOrdenMeritoDTO[];
  returnFocusRef?: RefObject<HTMLElement | null>;
  onEmitir: (evaluacionIds?: number[]) => void;
  onCancelar: () => void;
}

const ModalSeleccionarEvaluacionesContent: React.FC<
  ModalSeleccionarEvaluacionesContentProps
> = ({ puesto, evaluaciones, returnFocusRef, onEmitir, onCancelar }) => {
  const [todasSeleccionadas, setTodasSeleccionadas] = useState<boolean>(true);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const dialogRef = useRef<HTMLDivElement>(null);
  const todasCheckboxRef = useRef<HTMLInputElement>(null);

  useModalFocusTrap({
    isOpen: true,
    containerRef: dialogRef,
    initialFocusRef: todasCheckboxRef,
    returnFocusRef,
    onClose: onCancelar,
  });

  const handleToggleTodas = (checked: boolean) => {
    setTodasSeleccionadas(checked);
    if (checked) {
      setSelectedIds([]);
    }
  };

  const handleToggleEvaluacion = (evaluacionId: number, checked: boolean) => {
    if (checked) {
      setTodasSeleccionadas(false);
      setSelectedIds((prev) =>
        prev.includes(evaluacionId) ? prev : [...prev, evaluacionId]
      );
    } else {
      setSelectedIds((prev) => prev.filter((id) => id !== evaluacionId));
    }
  };

  const haySeleccion = todasSeleccionadas || selectedIds.length > 0;

  const handleConfirmarEmitir = () => {
    if (!haySeleccion) return;
    if (todasSeleccionadas) {
      onEmitir(undefined);
    } else {
      onEmitir(selectedIds);
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-seleccionar-evaluaciones-titulo"
      aria-describedby="modal-seleccionar-evaluaciones-desc"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onCancelar();
        }
      }}
    >
      <div
        ref={dialogRef}
        tabIndex={-1}
        className="relative w-full max-w-xl bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-7 flex flex-col gap-5 max-h-[90vh]"
      >
        {/* Cabecera */}
        <div className="flex items-start justify-between gap-4 border-b border-[#EADDFF] pb-4">
          <div className="flex flex-col gap-1">
            <h2
              id="modal-seleccionar-evaluaciones-titulo"
              className="text-xl font-bold text-[#3F2581]"
            >
              Seleccionar evaluaciones
            </h2>
            <p
              id="modal-seleccionar-evaluaciones-desc"
              className="text-sm text-[#4A4A4A]"
            >
              <span className="font-semibold text-[#3F2581]">{puesto.codigo}</span>
              {' — '}
              <span className="font-semibold">{puesto.nombre}</span>
              {' · '}
              <span className="text-[#757575]">{puesto.empresa}</span>
            </p>
          </div>

          <button
            type="button"
            onClick={onCancelar}
            aria-label="Cerrar diálogo"
            title="Cerrar"
            className="p-1.5 rounded-lg text-[#757575] hover:text-[#3F2581] hover:bg-[#F4F0FF] transition-colors cursor-pointer"
          >
            <Icon name="close" size={20} />
          </button>
        </div>

        {/* Lista de opciones */}
        <div className="flex flex-col gap-3 overflow-y-auto pr-1">
          {/* Opción TODAS */}
          <label
            htmlFor="eval-opcion-todas"
            className={`flex items-center justify-between gap-3 p-3.5 rounded-xl border-2 transition-colors cursor-pointer select-none ${
              todasSeleccionadas
                ? 'bg-[#F4F0FF] border-[#3F2581]'
                : 'bg-white border-[#E0E0E0] hover:border-[#896ECB]'
            }`}
          >
            <span className="flex items-center gap-3">
              <input
                ref={todasCheckboxRef}
                id="eval-opcion-todas"
                type="checkbox"
                checked={todasSeleccionadas}
                onChange={(e) => handleToggleTodas(e.target.checked)}
                className="h-5 w-5 rounded border-[#3F2581] accent-[#3F2581] cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#3F2581]"
              />
              <span className="flex flex-col">
                <span className="text-sm md:text-base font-bold text-[#3F2581]">
                  TODAS
                </span>
                <span className="text-xs text-[#757575]">
                  Incluir todas las evaluaciones asociadas al puesto ({evaluaciones.length})
                </span>
              </span>
            </span>
          </label>

          {/* Separador */}
          <div className="flex items-center gap-2 pt-1">
            <span className="text-xs font-bold uppercase tracking-wider text-[#757575]">
              O seleccione evaluaciones específicas
            </span>
            <div className="flex-1 h-px bg-[#EADDFF]" />
          </div>

          {/* Lista individual de evaluaciones */}
          <fieldset className="flex flex-col gap-2">
            <legend className="sr-only">Evaluaciones individuales del puesto</legend>
            {evaluaciones.map((ev) => {
              const checkboxId = `eval-opcion-${ev.evaluacionId}`;
              const isChecked = selectedIds.includes(ev.evaluacionId);

              return (
                <label
                  key={ev.evaluacionId}
                  htmlFor={checkboxId}
                  className={`flex items-center justify-between gap-3 p-3.5 rounded-xl border transition-colors cursor-pointer select-none ${
                    isChecked
                      ? 'bg-[#F4F0FF] border-[#3F2581]'
                      : 'bg-white border-[#E0E0E0] hover:border-[#896ECB]'
                  }`}
                >
                  <span className="flex items-center gap-3 min-w-0">
                    <input
                      id={checkboxId}
                      type="checkbox"
                      checked={isChecked}
                      onChange={(e) =>
                        handleToggleEvaluacion(ev.evaluacionId, e.target.checked)
                      }
                      className="h-5 w-5 rounded border-[#3F2581] accent-[#3F2581] cursor-pointer shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#3F2581]"
                    />
                    <span className="flex flex-col min-w-0">
                      <span className="text-sm font-semibold text-[#1C1B1F]">
                        Evaluación #{ev.evaluacionId}
                      </span>
                      <span className="text-xs text-[#757575]">
                        Creada: {formatFechaLocal(ev.fechaCreacion)}
                      </span>
                    </span>
                  </span>

                  <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-[#FBF9FE] text-[#3F2581] border border-[#D0BCFF] shrink-0">
                    {ev.cantidadCompletos} de {ev.cantidadCandidatos} completos
                  </span>
                </label>
              );
            })}
          </fieldset>

          {/* Aviso de selección vacía */}
          {!haySeleccion && (
            <div
              role="alert"
              className="flex items-center gap-1.5 text-sm font-semibold text-[#D32F2F] pt-1"
            >
              <Icon name="error" size={16} className="text-[#D32F2F] shrink-0" />
              <span>{MENSAJE_SELECCION_VACIA}</span>
            </div>
          )}
        </div>

        {/* Acciones del modal */}
        <div className="flex items-center justify-end gap-3 border-t border-[#EADDFF] pt-4">
          <Button type="button" variant="secondary" onClick={onCancelar}>
            Cancelar
          </Button>
          <Button
            type="button"
            variant="primary"
            disabled={!haySeleccion}
            leftIcon="format_list_numbered"
            onClick={handleConfirmarEmitir}
          >
            Emitir
          </Button>
        </div>
      </div>
    </div>
  );
};

export const ModalSeleccionarEvaluaciones: React.FC<
  ModalSeleccionarEvaluacionesProps
> = ({ isOpen, puesto, evaluaciones, returnFocusRef, onEmitir, onCancelar }) => {
  if (!isOpen || !puesto) return null;

  return (
    <ModalSeleccionarEvaluacionesContent
      key={puesto.puestoId}
      puesto={puesto}
      evaluaciones={evaluaciones}
      returnFocusRef={returnFocusRef}
      onEmitir={onEmitir}
      onCancelar={onCancelar}
    />
  );
};

export default ModalSeleccionarEvaluaciones;
