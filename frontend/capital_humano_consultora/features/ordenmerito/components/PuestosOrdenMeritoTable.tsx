'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/molecules/EmptyState';
import type { PuestoOrdenMeritoDTO } from '../types';

export interface PuestosOrdenMeritoTableProps {
  puestos: PuestoOrdenMeritoDTO[];
  isLoading: boolean;
  loadingPuestoId?: number | null;
  onEmitirOrdenMerito: (
    puesto: PuestoOrdenMeritoDTO,
    triggerEl: HTMLButtonElement | null
  ) => void;
}

export const PuestosOrdenMeritoTable: React.FC<PuestosOrdenMeritoTableProps> = ({
  puestos,
  isLoading,
  loadingPuestoId = null,
  onEmitirOrdenMerito,
}) => {
  if (isLoading) {
    return (
      <div
        role="status"
        aria-live="polite"
        className="w-full bg-white rounded-xl border border-[#E0E0E0] p-10 flex flex-col items-center justify-center gap-3"
      >
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        <span className="text-sm text-[#757575] font-medium">
          Buscando puestos...
        </span>
      </div>
    );
  }

  if (puestos.length === 0) {
    return (
      <EmptyState
        icon="search"
        title="No se encontraron puestos"
        description="No existen puestos activos que coincidan con los criterios de búsqueda ingresados."
        className="my-2"
      />
    );
  }

  return (
    <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white shadow-xs">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF]">
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase"
              >
                Código
              </th>
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase"
              >
                Nombre del puesto
              </th>
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase"
              >
                Empresa
              </th>
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase text-center"
              >
                Candidatos
              </th>
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase text-center"
              >
                Evaluaciones Completas
              </th>
              <th
                scope="col"
                className="px-5 py-3.5 text-xs md:text-sm font-bold tracking-wider uppercase text-right"
              >
                Acción
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EADDFF]/60 text-sm">
            {puestos.map((puesto) => {
              const isRowLoading = loadingPuestoId === puesto.puestoId;
              const isAnyRowLoading = loadingPuestoId !== null;

              return (
                <tr
                  key={puesto.puestoId}
                  className="hover:bg-[#F4F0FF]/60 transition-colors text-[#333333]"
                >
                  <td className="px-5 py-3.5 font-semibold text-[#3F2581] whitespace-nowrap">
                    {puesto.codigo}
                  </td>
                  <td className="px-5 py-3.5 font-semibold text-[#1C1B1F]">
                    {puesto.nombre}
                  </td>
                  <td className="px-5 py-3.5 text-[#333333]">
                    {puesto.empresa}
                  </td>
                  <td className="px-5 py-3.5 text-center font-medium text-[#1C1B1F] tabular-nums">
                    {puesto.cantidadCandidatos}
                  </td>
                  <td className="px-5 py-3.5 text-center whitespace-nowrap">
                    <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]">
                      {puesto.evaluacionesCompletas} de {puesto.evaluacionesTotal}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right whitespace-nowrap">
                    <Button
                      type="button"
                      variant="primary"
                      size="sm"
                      leftIcon="format_list_numbered"
                      isLoading={isRowLoading}
                      disabled={isAnyRowLoading}
                      title="Emitir el orden de mérito"
                      onClick={(e) => onEmitirOrdenMerito(puesto, e.currentTarget)}
                      className="font-semibold"
                    >
                      Emitir el orden de mérito
                    </Button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PuestosOrdenMeritoTable;
