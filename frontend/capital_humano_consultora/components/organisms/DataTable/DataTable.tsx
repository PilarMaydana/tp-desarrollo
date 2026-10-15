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

  return (
    <div className={`w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white shadow-xs ${className}`}>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-[#3F2581] text-white">
              {columns.map((col, index) => (
                <th
                  key={index}
                  style={{ width: col.width }}
                  className="px-6 py-4 text-sm font-semibold tracking-wider uppercase border-b border-[#4E309D]"
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EADDFF]/50 text-sm">
            {data.map((row, rowIndex) => {
              const id = keyExtractor(row);
              const isSelected = selectedId === id;

              return (
                <tr
                  key={id}
                  onClick={() => onSelectRow && onSelectRow(row)}
                  className={`transition-colors cursor-pointer select-none ${
                    isSelected
                      ? 'bg-[#EADDFF] font-semibold text-[#1C1B1F]'
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
