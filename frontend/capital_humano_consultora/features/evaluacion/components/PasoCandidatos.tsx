'use client';

import React, { useMemo } from 'react';
import type { UseFormRegister } from 'react-hook-form';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type { BuscarCandidatosFormData } from '../schemas/buscarCandidatos.schema';
import type { CandidatoResumenDTO, ErrorPanelState } from '../types';
import { CandidatosSelectionTable } from './CandidatosSelectionTable';

export interface PasoCandidatosProps {
  register: UseFormRegister<BuscarCandidatosFormData>;
  onBuscarSubmit: (e?: React.BaseSyntheticEvent) => Promise<void> | void;
  isSearching: boolean;
  hasSearched: boolean;
  searchError: ErrorPanelState | null;
  searchEmptyMessage?: string | null;
  onDismissSearchError: () => void;
  resultadosBusqueda: CandidatoResumenDTO[];
  selectedResultadoIds: number[];
  onToggleResultado: (candidatoId: number) => void;
  onToggleAllResultados: (checked: boolean) => void;
  onAgregar: () => void;
  candidatosAEvaluar: CandidatoResumenDTO[];
  selectedAEvaluarIds: number[];
  onToggleAEvaluar: (candidatoId: number) => void;
  onToggleAllAEvaluar: (checked: boolean) => void;
  onQuitar: () => void;
  step1Error: ErrorPanelState | null;
  blockedNros: Set<string>;
  onDismissStep1Error: () => void;
  isValidating: boolean;
  onSiguiente: () => void;
}

/**
 * Normaliza a MAYÚSCULAS el valor de un input preservando la posición del cursor.
 */
function toUpperPreservingCaret(e: React.ChangeEvent<HTMLInputElement>) {
  const { selectionStart, selectionEnd, value } = e.target;
  const upper = value.toUpperCase();
  if (upper !== value) {
    e.target.value = upper;
    if (selectionStart !== null && selectionEnd !== null) {
      e.target.setSelectionRange(selectionStart, selectionEnd);
    }
  }
}

export const PasoCandidatos: React.FC<PasoCandidatosProps> = ({
  register,
  onBuscarSubmit,
  isSearching,
  hasSearched,
  searchError,
  searchEmptyMessage,
  onDismissSearchError,
  resultadosBusqueda,
  selectedResultadoIds,
  onToggleResultado,
  onToggleAllResultados,
  onAgregar,
  candidatosAEvaluar,
  selectedAEvaluarIds,
  onToggleAEvaluar,
  onToggleAllAEvaluar,
  onQuitar,
  step1Error,
  blockedNros,
  onDismissStep1Error,
  isValidating,
  onSiguiente,
}) => {
  const apellidoRegister = register('apellido', {
    onChange: toUpperPreservingCaret,
  });
  const nombreRegister = register('nombre', {
    onChange: toUpperPreservingCaret,
  });
  const nroCandidatoRegister = register('nroCandidato', {
    onChange: toUpperPreservingCaret,
  });

  const agregadosIds = useMemo(
    () => new Set(candidatosAEvaluar.map((c) => c.id)),
    [candidatosAEvaluar]
  );

  const isNoCandidatosError =
    Boolean(step1Error) &&
    (!step1Error?.detalles || step1Error.detalles.length === 0) &&
    (step1Error?.mensajeGeneral === 'Debe seleccionar uno o más candidatos' ||
      step1Error?.titulo === 'Selección requerida');

  return (
    <div className="flex flex-col gap-6">
      {/* Tarjeta de Búsqueda de Candidatos */}
      <section className="bg-white p-6 sm:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-lg sm:text-xl font-bold text-[#3F2581]">
            Búsqueda de Candidatos
          </h2>
          <p className="text-sm text-[#757575]">
            Filtre por apellido, nombre o número de candidato. Sin filtros se listarán todos los candidatos activos.
          </p>
        </div>

        <form
          onSubmit={onBuscarSubmit}
          className="flex flex-col md:flex-row gap-4 items-end"
          noValidate
        >
          <div className="w-full md:flex-1">
            <NotchedField label="Apellido">
              <Input
                {...apellidoRegister}
                placeholder="PÉREZ"
                autoComplete="off"
                className="uppercase"
              />
            </NotchedField>
          </div>

          <div className="w-full md:flex-1">
            <NotchedField label="Nombre">
              <Input
                {...nombreRegister}
                placeholder="JUAN"
                autoComplete="off"
                className="uppercase"
              />
            </NotchedField>
          </div>

          <div className="w-full md:w-44 shrink-0">
            <NotchedField label="Nro. de Candidato">
              <Input
                {...nroCandidatoRegister}
                placeholder="4"
                autoComplete="off"
                className="uppercase"
              />
            </NotchedField>
          </div>

          <div className="w-full md:w-auto shrink-0 pt-2 md:pt-0">
            <Button
              type="submit"
              variant="primary"
              isLoading={isSearching}
              leftIcon="search"
              className="w-full md:w-auto px-6 h-[46px]"
            >
              Buscar
            </Button>
          </div>
        </form>

        {/* Mensaje cuando no existen candidatos o error de búsqueda: solo texto en rojo, sin recuadro */}
        {searchEmptyMessage && (
          <div className="flex items-center gap-1.5 text-sm font-semibold text-[#D32F2F] pt-1 animate-in fade-in duration-150">
            <Icon name="error" size={16} className="text-[#D32F2F] shrink-0" />
            <span>{searchEmptyMessage}</span>
          </div>
        )}

        {/* Resultados de búsqueda */}
        <div className="flex flex-col gap-3 pt-2">
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold text-[#3F2581]">
              Resultados de la búsqueda
            </h3>
            {hasSearched && !isSearching && (
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#EADDFF] text-[#3F2581]">
                {resultadosBusqueda.length}
              </span>
            )}
          </div>

          {!hasSearched && !isSearching ? (
            <div className="rounded-xl border border-dashed border-[#D0BCFF] bg-[#FBF9FE] px-6 py-8 text-center text-sm text-[#757575]">
              Ingrese criterios opcionales y presione <strong>Buscar</strong> para consultar los candidatos disponibles.
            </div>
          ) : searchEmptyMessage ? (
            <div className="rounded-xl border border-dashed border-[#D0BCFF] bg-[#FBF9FE] px-6 py-8 text-center text-sm text-[#757575]">
              Sin resultados para los filtros ingresados. Modifique los criterios y vuelva a intentar.
            </div>
          ) : (
            <CandidatosSelectionTable
              idPrefix="resultados-candidatos"
              candidatos={resultadosBusqueda}
              selectedIds={selectedResultadoIds}
              onToggleOne={onToggleResultado}
              onToggleAll={onToggleAllResultados}
              disabledIds={agregadosIds}
              disabledBadgeLabel="Ya agregado"
              isLoading={isSearching}
              emptyTitle="No se encontraron candidatos"
              emptyDescription="No existen candidatos activos que coincidan con los criterios ingresados."
              emptyIcon="search"
              footerAction={
                <Button
                  type="button"
                  variant="secondary-soft"
                  size="sm"
                  leftIcon="plus"
                  onClick={onAgregar}
                  disabled={selectedResultadoIds.length === 0 || isSearching || isValidating}
                >
                  Agregar{selectedResultadoIds.length > 0 ? ` (${selectedResultadoIds.length})` : ''}
                </Button>
              }
            />
          )}
        </div>
      </section>

      {/* Panel visible de validación de backend (6.A / 400 / red) con detalles múltiples */}
      {step1Error && !isNoCandidatosError && (
        <AlertaErrores
          titulo={step1Error.titulo || 'No es posible continuar'}
          mensajeGeneral={step1Error.mensajeGeneral}
          errores={step1Error.detalles}
          onDismiss={onDismissStep1Error}
        />
      )}

      {/* Grilla de Candidatos a evaluar */}
      <section className="bg-white p-6 sm:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <h2 className="text-lg sm:text-xl font-bold text-[#3F2581]">
              Candidatos a evaluar
            </h2>
            <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#EADDFF] text-[#3F2581]">
              {candidatosAEvaluar.length}
            </span>
          </div>

          {step1Error && isNoCandidatosError && (
            <span className="text-xs sm:text-sm font-semibold text-[#D32F2F] flex items-center gap-1.5 animate-in fade-in duration-150">
              <Icon name="error" size={16} className="text-[#D32F2F] shrink-0" />
              {step1Error.mensajeGeneral || 'Debe seleccionar uno o más candidatos'}
            </span>
          )}
        </div>

        <CandidatosSelectionTable
          idPrefix="candidatos-evaluar"
          candidatos={candidatosAEvaluar}
          selectedIds={selectedAEvaluarIds}
          onToggleOne={onToggleAEvaluar}
          onToggleAll={onToggleAllAEvaluar}
          blockedNros={blockedNros}
          emptyTitle="No hay candidatos agregados"
          emptyDescription="Seleccione uno o más candidatos de los resultados de búsqueda y presione «Agregar»."
          emptyIcon="groups"
          footerAction={
            <Button
              type="button"
              variant="secondary"
              size="sm"
              leftIcon="trash"
              onClick={onQuitar}
              disabled={selectedAEvaluarIds.length === 0 || isValidating}
            >
              Quitar{selectedAEvaluarIds.length > 0 ? ` (${selectedAEvaluarIds.length})` : ''}
            </Button>
          }
        />
      </section>

      {/* Barra de Acciones del Paso 1 */}
      <div className="flex flex-col gap-2">
        {step1Error && isNoCandidatosError && (
          <div className="flex items-center justify-end gap-1.5 text-sm font-semibold text-[#D32F2F] animate-in fade-in duration-150 px-2">
            <Icon name="error" size={18} className="text-[#D32F2F] shrink-0" />
            <span>{step1Error.mensajeGeneral || 'Debe seleccionar uno o más candidatos'}</span>
          </div>
        )}

        <ActionToolbar align="right">
          <Button
            type="button"
            variant="primary"
            rightIcon="arrow_forward"
            isLoading={isValidating}
            onClick={onSiguiente}
          >
            Siguiente
          </Button>
        </ActionToolbar>
      </div>
    </div>
  );
};

export default PasoCandidatos;
