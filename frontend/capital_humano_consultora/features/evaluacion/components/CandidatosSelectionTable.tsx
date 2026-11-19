'use client';

import React from 'react';
import { Checkbox } from '@/components/ui/Checkbox';
import { EmptyState } from '@/components/molecules/EmptyState';
import type { CandidatoResumenDTO } from '../types';

export interface CandidatosSelectionTableProps {
  idPrefix: string;
  candidatos: CandidatoResumenDTO[];
  selectedIds: number[];
  onToggleOne: (candidatoId: number) => void;
  onToggleAll: (checked: boolean) => void;
  disabledIds?: Set<number>;
  disabledBadgeLabel?: string;
  blockedNros?: Set<string>;
  isLoading?: boolean;
  emptyTitle: string;
  emptyDescription?: string;
  emptyIcon?: string;
}

export const CandidatosSelectionTable: React.FC<CandidatosSelectionTableProps> = ({
  idPrefix,
  candidatos,
  selectedIds,
  onToggleOne,
  onToggleAll,
  disabledIds,
  disabledBadgeLabel = 'Ya agregado',
  blockedNros,
  isLoading = false,
  emptyTitle,
  emptyDescription,
  emptyIcon = 'search',
}) => {
  if (isLoading) {
    return (
      <div className="w-full bg-white rounded-xl border border-[#D0BCFF] p-8 flex flex-col items-center justify-center gap-3">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        <span className="text-sm text-[#757575] font-medium">Buscando candidatos...</span>
      </div>
    );
  }

  if (candidatos.length === 0) {
    return (
      <EmptyState
        icon={emptyIcon}
        title={emptyTitle}
        description={emptyDescription}
        className="my-2"
      />
    );
  }

  const selectableCandidatos = candidatos.filter((c) => !disabledIds?.has(c.id));
  const selectedSet = new Set(selectedIds);
  const allSelectableChecked =
    selectableCandidatos.length > 0 &&
    selectableCandidatos.every((c) => selectedSet.has(c.id));

  return (
    <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white shadow-xs">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF]">
              <th className="px-4 py-3.5 w-14">
                <div className="inline-flex rounded-md focus-within:ring-2 focus-within:ring-[#3F2581] focus-within:ring-offset-1">
                  <Checkbox
                    id={`${idPrefix}-select-all`}
                    checked={allSelectableChecked}
                    disabled={selectableCandidatos.length === 0}
                    onChange={onToggleAll}
                  />
                </div>
              </th>
              <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                Nro. candidato
              </th>
              <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                Apellido
              </th>
              <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                Nombre
              </th>
              <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                Tipo documento
              </th>
              <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                Nro. documento
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EADDFF]/50 text-sm">
            {candidatos.map((candidato) => {
              const isDisabled = disabledIds?.has(candidato.id) ?? false;
              const isSelected = !isDisabled && selectedSet.has(candidato.id);
              const isBlocked = blockedNros?.has(candidato.nroCandidato) ?? false;

              return (
                <tr
                  key={candidato.id}
                  onClick={() => {
                    if (!isDisabled) {
                      onToggleOne(candidato.id);
                    }
                  }}
                  className={`transition-colors select-none ${
                    isDisabled
                      ? 'bg-[#F6F6F6] text-[#757575] cursor-default'
                      : isBlocked
                      ? 'bg-red-50/80 hover:bg-red-50 text-[#1C1B1F] cursor-pointer'
                      : isSelected
                      ? 'bg-[#F4F0FF] font-medium text-[#1C1B1F] cursor-pointer'
                      : 'hover:bg-[#F4F0FF]/50 text-[#333333] cursor-pointer'
                  }`}
                >
                  <td
                    className="px-4 py-3"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div className="inline-flex rounded-md focus-within:ring-2 focus-within:ring-[#3F2581] focus-within:ring-offset-1">
                      <Checkbox
                        id={`${idPrefix}-row-${candidato.id}`}
                        checked={isSelected}
                        disabled={isDisabled}
                        onChange={() => onToggleOne(candidato.id)}
                      />
                    </div>
                  </td>
                  <td className="px-4 py-3 font-medium">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span>{candidato.nroCandidato}</span>
                      {isDisabled && (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-[#EADDFF] text-[#3F2581]">
                          {disabledBadgeLabel}
                        </span>
                      )}
                      {isBlocked && (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-[#D32F2F]">
                          No habilitado
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3">{candidato.apellido}</td>
                  <td className="px-4 py-3">{candidato.nombre}</td>
                  <td className="px-4 py-3">{candidato.tipoDocumento}</td>
                  <td className="px-4 py-3">{candidato.nroDocumento}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default CandidatosSelectionTable;
