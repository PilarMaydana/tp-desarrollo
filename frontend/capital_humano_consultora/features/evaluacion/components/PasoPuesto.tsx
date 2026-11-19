'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Select, SelectOption } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type {
  CompetenciaRequeridaDTO,
  DetallePuestoEvaluacionDTO,
  ErrorPanelState,
  PuestoResumenDTO,
} from '../types';

export interface PasoPuestoProps {
  cantidadCandidatos: number;
  puestos: PuestoResumenDTO[];
  isLoadingPuestos: boolean;
  puestosError: ErrorPanelState | null;
  onReintentarPuestos: () => void;
  selectedPuestoId: string;
  onSelectPuesto: (puestoId: string) => void;
  detallePuesto: DetallePuestoEvaluacionDTO | null;
  isLoadingCompetencias: boolean;
  step2Error: ErrorPanelState | null;
  onDismissStep2Error: () => void;
  isLoadingPreview: boolean;
  onAnterior: () => void;
  onSiguiente: () => void;
}

const COMPETENCIAS_COLUMNS: Column<CompetenciaRequeridaDTO>[] = [
  {
    header: 'Código',
    accessor: 'codigo',
    width: '20%',
  },
  {
    header: 'Nombre',
    accessor: 'nombre',
    width: '55%',
  },
  {
    header: 'Ponderación requerida',
    accessor: 'ponderacionRequerida',
    width: '25%',
    render: (row) => (
      <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-[#EADDFF] text-[#3F2581]">
        {row.ponderacionRequerida}
      </span>
    ),
  },
];

export const PasoPuesto: React.FC<PasoPuestoProps> = ({
  cantidadCandidatos,
  puestos,
  isLoadingPuestos,
  puestosError,
  onReintentarPuestos,
  selectedPuestoId,
  onSelectPuesto,
  detallePuesto,
  isLoadingCompetencias,
  step2Error,
  onDismissStep2Error,
  isLoadingPreview,
  onAnterior,
  onSiguiente,
}) => {
  const puestoOptions: SelectOption[] = useMemo(
    () =>
      puestos.map((p) => ({
        value: String(p.id),
        label: `${p.nombre} — ${p.empresa}`,
      })),
    [puestos]
  );

  return (
    <div className="flex flex-col gap-6">
      <section className="bg-white p-6 sm:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="flex flex-col gap-1">
            <h2 className="text-lg sm:text-xl font-bold text-[#3F2581]">
              Selección de Función o Puesto
            </h2>
            <p className="text-sm text-[#757575]">
              Seleccione el puesto en el cual desea evaluar a los candidatos. Se mostrarán sus competencias y ponderaciones requeridas.
            </p>
          </div>

          <span className="inline-flex items-center px-3 py-1.5 rounded-full text-xs font-semibold bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]">
            {cantidadCandidatos}{' '}
            {cantidadCandidatos === 1 ? 'candidato seleccionado' : 'candidatos seleccionados'}
          </span>
        </div>

        {/* Error al cargar puestos */}
        {puestosError && (
          <div className="flex flex-col gap-3">
            <AlertaErrores
              titulo={puestosError.titulo || 'Error al cargar los puestos'}
              mensajeGeneral={puestosError.mensajeGeneral}
              errores={puestosError.detalles}
            />
            <div>
              <Button
                type="button"
                variant="secondary-soft"
                size="sm"
                onClick={onReintentarPuestos}
                disabled={isLoadingPuestos}
              >
                Reintentar carga de puestos
              </Button>
            </div>
          </div>
        )}

        {/* Desplegable de puestos */}
        <NotchedField label="Función o puesto" required>
          <Select
            aria-label="Función o puesto"
            options={puestoOptions}
            placeholder={
              isLoadingPuestos
                ? 'Cargando puestos disponibles...'
                : 'Seleccione una función o puesto'
            }
            value={selectedPuestoId}
            disabled={isLoadingPuestos || isLoadingPreview}
            onChange={(e) => onSelectPuesto(e.target.value)}
          />
        </NotchedField>

        {/* Error local (10.A) o error del backend al consultar competencias / previsualizar */}
        {step2Error && (
          <AlertaErrores
            titulo={step2Error.titulo || 'No es posible continuar'}
            mensajeGeneral={step2Error.mensajeGeneral}
            errores={step2Error.detalles}
            onDismiss={onDismissStep2Error}
          />
        )}

        {/* Tabla informativa de competencias y ponderación requerida */}
        {selectedPuestoId && (isLoadingCompetencias || detallePuesto) && (
          <div className="flex flex-col gap-3 pt-2">
            {detallePuesto && !isLoadingCompetencias && (
              <div className="flex flex-wrap items-center justify-between gap-2 p-4 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF]">
                <div className="flex flex-col">
                  <span className="text-xs font-semibold uppercase tracking-wider text-[#4F378A]">
                    Puesto seleccionado
                  </span>
                  <span className="text-base font-bold text-[#3F2581]">
                    {detallePuesto.nombre} — {detallePuesto.empresa}
                  </span>
                </div>
                <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-white text-[#3F2581] border border-[#D0BCFF]">
                  Código: {detallePuesto.codigo}
                </span>
              </div>
            )}

            <h3 className="text-base font-bold text-[#3F2581]">
              Competencias requeridas
            </h3>

            <DataTable<CompetenciaRequeridaDTO>
              columns={COMPETENCIAS_COLUMNS}
              data={detallePuesto?.competencias ?? []}
              keyExtractor={(item) => String(item.competenciaId)}
              isLoading={isLoadingCompetencias}
              emptyTitle="Sin competencias asociadas"
              emptyDescription="El puesto seleccionado no posee competencias registradas."
              variant="soft-purple"
            />
          </div>
        )}
      </section>

      {/* Barra de Acciones del Paso 2 */}
      <ActionToolbar align="between">
        <Button
          type="button"
          variant="secondary"
          leftIcon="arrow_back"
          disabled={isLoadingPreview}
          onClick={onAnterior}
        >
          Anterior
        </Button>

        <Button
          type="button"
          variant="primary"
          rightIcon="arrow_forward"
          isLoading={isLoadingPreview}
          disabled={isLoadingPuestos || isLoadingCompetencias}
          onClick={onSiguiente}
        >
          Siguiente
        </Button>
      </ActionToolbar>
    </div>
  );
};

export default PasoPuesto;
