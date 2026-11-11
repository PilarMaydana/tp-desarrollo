'use client';

import React from 'react';
import { EmptyState } from '@/components/molecules/EmptyState';

export interface Column<T> {
  header: string;
  accessor: keyof T | string;
  width?: string;
  render?: (row: T, index: number) => React.ReactNode;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor: (item: T) => string;
  selectedId?: string | null;
  onSelectRow?: (item: T) => void;
  isLoading?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
  variant?: 'purple' | 'neutral' | 'soft-purple';
  className?: string;
}

export function DataTable<T>({
  columns,
  data,
  keyExtractor,
  selectedId,
  onSelectRow,
  isLoading = false,
  emptyTitle = 'No hay registros',
  emptyDescription = 'No se encontraron filas que coincidan con la búsqueda.',
  variant = 'soft-purple',
  className = '',
}: DataTableProps<T>) {
  if (isLoading) {
    return (
      <div className="w-full bg-white rounded-xl border border-[#E0E0E0] p-8 flex flex-col items-center justify-center gap-3">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        <span className="text-sm text-[#757575] font-medium">Cargando datos...</span>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <EmptyState
        icon="search"
        title={emptyTitle}
        description={emptyDescription}
        className="my-4"
      />
    );
  }

  const isSoftPurple = variant === 'soft-purple';
  const isNeutral = variant === 'neutral';

  return (
    <div
      className={`w-full overflow-hidden rounded-xl border ${
        isNeutral ? 'border-[#E0E0E0]' : 'border-[#D0BCFF]'
      } bg-white shadow-xs ${className}`}
    >
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr
              className={
                isSoftPurple
                  ? 'bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF]'
                  : isNeutral
                  ? 'bg-[#F9FAFB] text-[#475467] border-b border-[#EAECF0]'
                  : 'bg-[#3F2581] text-white border-b border-[#4E309D]'
              }
            >
              {columns.map((col, index) => (
                <th
                  key={index}
                  style={{ width: col.width }}
                  className={`px-6 py-4 text-base font-bold tracking-wider uppercase ${
                    isSoftPurple
                      ? 'text-[#3F2581]'
                      : isNeutral
                      ? 'text-[#475467]'
                      : 'text-white'
                  }`}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody
            className={`divide-y ${
              isNeutral ? 'divide-[#EAECF0]' : 'divide-[#EADDFF]/50'
            } text-sm`}
          >
            {data.map((row, rowIndex) => {
              const id = keyExtractor(row);
              const isSelected = selectedId === id;

              return (
                <tr
                  key={id}
                  onClick={() => onSelectRow && onSelectRow(row)}
                  className={`transition-colors cursor-pointer select-none ${
                    isSelected
                      ? isSoftPurple
                        ? 'bg-[#FBF9FE] font-medium text-[#1C1B1F]'
                        : isNeutral
                        ? 'bg-[#F2F4F7] font-medium text-[#101828]'
                        : 'bg-[#EADDFF] font-semibold text-[#1C1B1F]'
                      : isSoftPurple
                      ? 'hover:bg-[#F4F0FF]/60 text-[#333333]'
                      : isNeutral
                      ? 'hover:bg-[#F9FAFB] text-[#344054]'
                      : 'hover:bg-[#F4F0FF] text-[#333333]'
                  }`}
                >
                  {columns.map((col, colIndex) => {
                    const value = (row as Record<string, unknown>)[col.accessor as string];
                    return (
                      <td key={colIndex} className="px-6 py-3.5">
                        {col.render ? col.render(row, rowIndex) : String(value ?? '-')}
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default DataTable;
