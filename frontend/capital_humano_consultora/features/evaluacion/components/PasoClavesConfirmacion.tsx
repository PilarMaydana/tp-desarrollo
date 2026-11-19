'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type {
  CandidatoClaveDTO,
  ErrorPanelState,
  PrevisualizacionEvaluacionDTO,
} from '../types';

export interface PasoClavesConfirmacionProps {
  previsualizacion: PrevisualizacionEvaluacionDTO;
  step3Error: ErrorPanelState | null;
  onDismissStep3Error: () => void;
  isFinalizing: boolean;
  isCreated: boolean;
  onAnterior: () => void;
  onCancelar: () => void;
  onFinalizar: () => void;
}

const CLAVES_COLUMNS: Column<CandidatoClaveDTO>[] = [
  {
    header: 'Apellido',
    accessor: 'apellido',
    width: '24%',
  },
  {
    header: 'Nombre',
    accessor: 'nombre',
    width: '24%',
  },
  {
    header: 'Tipo documento',
    accessor: 'tipoDocumento',
    width: '16%',
  },
  {
    header: 'Nro. documento',
    accessor: 'nroDocumento',
    width: '18%',
  },
  {
    header: 'Clave de ingreso',
    accessor: 'claveAcceso',
    width: '18%',
    render: (row) => (
      <span className="inline-flex items-center px-3 py-1 rounded-lg bg-[#F4F0FF] border border-[#D0BCFF] font-mono text-sm font-bold tracking-widest text-[#3F2581]">
        {row.claveAcceso}
      </span>
    ),
  },
];

export const PasoClavesConfirmacion: React.FC<PasoClavesConfirmacionProps> = ({
  previsualizacion,
  step3Error,
  onDismissStep3Error,
  isFinalizing,
  isCreated,
  onAnterior,
  onCancelar,
  onFinalizar,
}) => {
  return (
    <div className="flex flex-col gap-6">
      {/* Encabezado informativo con parámetros de la evaluación */}
      <section className="bg-white p-6 sm:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-lg sm:text-xl font-bold text-[#3F2581]">
            Confirmación y Claves de Ingreso
          </h2>
          <p className="text-sm text-[#757575]">
            Verifique el puesto, los parámetros del cuestionario y las claves generadas para cada candidato antes de finalizar.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF] flex flex-col gap-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4F378A] flex items-center gap-1.5">
              <Icon name="work" size={14} />
              Puesto
            </span>
            <span className="text-sm font-bold text-[#3F2581]">
              {previsualizacion.puestoNombre}
            </span>
            <span className="text-xs text-[#757575]">
              Código: {previsualizacion.puestoCodigo}
            </span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF] flex flex-col gap-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4F378A] flex items-center gap-1.5">
              <Icon name="assignment" size={14} />
              Empresa
            </span>
            <span className="text-sm font-bold text-[#3F2581]">
              {previsualizacion.empresa}
            </span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF] flex flex-col gap-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4F378A] flex items-center gap-1.5">
              <Icon name="timer" size={14} />
              Tiempo máximo
            </span>
            <span className="text-sm font-bold text-[#3F2581]">
              {previsualizacion.tiempoMaximoMinutos} minutos
            </span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF] flex flex-col gap-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4F378A] flex items-center gap-1.5">
              <Icon name="file_question" size={14} />
              Preguntas por bloque
            </span>
            <span className="text-sm font-bold text-[#3F2581]">
              {previsualizacion.cantidadPreguntasPorBloque}
            </span>
          </div>
        </div>
      </section>

      {/* Error real del backend sin tapar la grilla ni los botones */}
      {step3Error && (
        <AlertaErrores
          titulo={step3Error.titulo || 'No fue posible crear la evaluación'}
          mensajeGeneral={step3Error.mensajeGeneral}
          errores={step3Error.detalles}
          onDismiss={onDismissStep3Error}
        />
      )}

      {/* Grilla de Candidatos a evaluar con claves */}
      <section className="bg-white p-6 sm:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <h3 className="text-lg font-bold text-[#3F2581]">
              Candidatos a evaluar
            </h3>
            <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#EADDFF] text-[#3F2581]">
              {previsualizacion.candidatos.length}
            </span>
          </div>
        </div>

        <DataTable<CandidatoClaveDTO>
          columns={CLAVES_COLUMNS}
          data={previsualizacion.candidatos}
          keyExtractor={(item) => String(item.candidatoId)}
          emptyTitle="Sin candidatos para evaluar"
          emptyDescription="No hay candidatos incluidos en la previsualización."
          variant="soft-purple"
        />
      </section>

      {/* Barra de Acciones del Paso 3 */}
      <ActionToolbar align="between">
        <Button
          type="button"
          variant="secondary"
          leftIcon="arrow_back"
          disabled={isFinalizing || isCreated}
          onClick={onAnterior}
        >
          Anterior
        </Button>

        <div className="flex flex-wrap items-center gap-3">
          <Button
            type="button"
            variant="outline"
            disabled={isFinalizing || isCreated}
            onClick={onCancelar}
          >
            Cancelar
          </Button>

          <Button
            type="button"
            variant="primary"
            rightIcon="check"
            isLoading={isFinalizing}
            disabled={isFinalizing || isCreated}
            onClick={onFinalizar}
          >
            Finalizar
          </Button>
        </div>
      </ActionToolbar>
    </div>
  );
};

export default PasoClavesConfirmacion;
