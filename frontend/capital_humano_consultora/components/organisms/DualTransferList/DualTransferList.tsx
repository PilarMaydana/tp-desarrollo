'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { EmptyState } from '@/components/molecules/EmptyState';
import { Icon } from '@/components/ui/Icon';

export interface TransferItem {
  id: string;
  label: string;
  subtitle?: string;
  weight?: number; // 0-10 si aplica
}

export interface DualTransferListProps {
  availableTitle?: string;
  selectedTitle?: string;
  availableItems: TransferItem[];
  selectedItems: TransferItem[];
  onChange: (newSelectedItems: TransferItem[]) => void;
  withWeights?: boolean; // Ponderaciones de 0 a 10
  className?: string;
}

export const DualTransferList: React.FC<DualTransferListProps> = ({
  availableTitle = 'Competencias disponibles',
  selectedTitle = 'Competencias seleccionadas',
  availableItems,
  selectedItems,
  onChange,
  withWeights = true,
  className = '',
}) => {
  // Items seleccionados temporalmente en el panel izquierdo para mover
  const [checkedAvailableIds, setCheckedAvailableIds] = useState<string[]>([]);
  // Items seleccionados temporalmente en el panel derecho para remover
  const [checkedSelectedIds, setCheckedSelectedIds] = useState<string[]>([]);

  // Filtramos los items disponibles que NO estén ya seleccionados
  const unselectedAvailable = availableItems.filter(
    (item) => !selectedItems.some((sel) => sel.id === item.id)
  );

  const handleToggleAvailable = (id: string) => {
    setCheckedAvailableIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const handleToggleSelectAllAvailable = (checked: boolean) => {
    if (checked) {
      setCheckedAvailableIds(unselectedAvailable.map((i) => i.id));
    } else {
      setCheckedAvailableIds([]);
    }
  };

  const handleToggleSelected = (id: string) => {
    setCheckedSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const handleAdd = () => {
    if (checkedAvailableIds.length === 0) return;
    const itemsToAdd: TransferItem[] = unselectedAvailable
      .filter((i) => checkedAvailableIds.includes(i.id))
      .map((i) => ({ ...i, weight: i.weight ?? 5 }));

    onChange([...selectedItems, ...itemsToAdd]);
    setCheckedAvailableIds([]);
  };

  const handleRemove = () => {
    if (checkedSelectedIds.length === 0) return;
    onChange(selectedItems.filter((i) => !checkedSelectedIds.includes(i.id)));
    setCheckedSelectedIds([]);
  };

  const handleRemoveSingle = (id: string) => {
    onChange(selectedItems.filter((i) => i.id !== id));
    setCheckedSelectedIds((prev) => prev.filter((i) => i !== id));
  };

  const handleWeightChange = (id: string, weightVal: number) => {
    const clamped = Math.max(0, Math.min(10, weightVal || 0));
    onChange(
      selectedItems.map((item) =>
        item.id === id ? { ...item, weight: clamped } : item
      )
    );
  };

  const allAvailableChecked =
    unselectedAvailable.length > 0 &&
    unselectedAvailable.every((i) => checkedAvailableIds.includes(i.id));

  return (
    <div className={`grid grid-cols-1 lg:grid-cols-2 gap-6 w-full ${className}`}>
      {/* PANEL IZQUIERDO: DISPONIBLES */}
      <div className="flex flex-col bg-[#F4F0FF] rounded-2xl border-2 border-[#D0BCFF] p-5 shadow-xs">
        <div className="flex items-center justify-between pb-3 border-b border-[#D0BCFF]/60 mb-3">
          <h3 className="text-lg font-bold text-[#3F2581]">{availableTitle}</h3>
          {unselectedAvailable.length > 0 && (
            <div className="flex items-center">
              <Checkbox
                checked={allAvailableChecked}
                onChange={handleToggleSelectAllAvailable}
                label={<span className="text-xs text-[#3F2581] font-semibold">Seleccionar todos</span>}
              />
            </div>
          )}
        </div>

        {/* Lista scrollable de disponibles */}
        <div className="flex-1 overflow-y-auto max-h-[360px] min-h-[220px] pr-2 space-y-2">
          {unselectedAvailable.length === 0 ? (
            <div className="flex items-center justify-center h-full py-12 text-[#757575] text-sm">
              Todas las opciones han sido seleccionadas
            </div>
          ) : (
            unselectedAvailable.map((item) => {
              const isChecked = checkedAvailableIds.includes(item.id);
              return (
                <div
                  key={item.id}
                  onClick={() => handleToggleAvailable(item.id)}
                  className={`flex items-start gap-3 p-3 rounded-xl border transition-all cursor-pointer ${
                    isChecked
                      ? 'bg-[#EADDFF] border-[#3F2581]'
                      : 'bg-white border-[#E0E0E0] hover:border-[#BDBEE0]'
                  }`}
                >
                  <Checkbox
                    checked={isChecked}
                    onChange={() => handleToggleAvailable(item.id)}
                  />
                  <div className="flex flex-col flex-1">
                    <span className="text-sm font-semibold text-[#000000]">{item.label}</span>
                    {item.subtitle && (
                      <span className="text-xs text-[#757575]">{item.subtitle}</span>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Pie del panel izquierdo: Botón Agregar */}
        <div className="pt-4 border-t border-[#D0BCFF]/60 mt-3 flex justify-end">
          <Button
            type="button"
            variant="secondary-soft"
            onClick={handleAdd}
            disabled={checkedAvailableIds.length === 0}
            leftIcon="plus"
          >
            Agregar ({checkedAvailableIds.length})
          </Button>
        </div>
      </div>

      {/* PANEL DERECHO: SELECCIONADOS */}
      <div className="flex flex-col bg-[#F4F0FF] rounded-2xl border-2 border-[#D0BCFF] p-5 shadow-xs">
        <div className="flex items-center justify-between pb-3 border-b border-[#D0BCFF]/60 mb-3">
          <h3 className="text-lg font-bold text-[#3F2581]">{selectedTitle}</h3>
          {withWeights && selectedItems.length > 0 && (
            <span className="text-xs font-semibold text-[#3F2581] bg-[#EADDFF] px-2.5 py-1 rounded-full">
              Ponderaciones (0-10)
            </span>
          )}
        </div>

        {/* Lista scrollable de seleccionados */}
        <div className="flex-1 overflow-y-auto max-h-[360px] min-h-[220px] pr-2 space-y-2">
          {selectedItems.length === 0 ? (
            <EmptyState
              icon="help_outline"
              title="Ninguna seleccionada aún"
              description="Seleccione elementos del panel izquierdo y presione 'Agregar'."
            />
          ) : (
            selectedItems.map((item) => {
              const isChecked = checkedSelectedIds.includes(item.id);
              return (
                <div
                  key={item.id}
                  className={`flex items-center justify-between gap-3 p-3 rounded-xl border transition-all ${
                    isChecked
                      ? 'bg-[#EADDFF] border-[#3F2581]'
                      : 'bg-white border-[#E0E0E0]'
                  }`}
                >
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <Checkbox
                      checked={isChecked}
                      onChange={() => handleToggleSelected(item.id)}
                    />
                    <span className="text-sm font-semibold text-[#000000] truncate">
                      {item.label}
                    </span>
                  </div>

                  {/* Input de Ponderación (0-10) si aplica */}
                  {withWeights && (
                    <div className="flex items-center gap-2 shrink-0">
                      <label className="text-xs text-[#757575] hidden sm:inline">Pond:</label>
                      <input
                        type="number"
                        min="0"
                        max="10"
                        value={item.weight ?? 0}
                        onChange={(e) => handleWeightChange(item.id, parseInt(e.target.value, 10))}
                        className="w-16 h-9 px-2 text-center text-sm font-bold text-[#3F2581] bg-[#F4F0FF] border-2 border-[#3F2581] rounded-lg outline-none focus:ring-2 focus:ring-[#896ECB]"
                      />
                    </div>
                  )}

                  {/* Botón de eliminación individual */}
                  <button
                    type="button"
                    onClick={() => handleRemoveSingle(item.id)}
                    className="p-1.5 text-[#D32F2F] hover:bg-red-50 rounded-lg transition-colors cursor-pointer"
                    title="Quitar"
                  >
                    <Icon name="trash" size={18} />
                  </button>
                </div>
              );
            })
          )}
        </div>

        {/* Pie del panel derecho: Botón Quitar */}
        <div className="pt-4 border-t border-[#D0BCFF]/60 mt-3 flex justify-end">
          <Button
            type="button"
            variant="secondary"
            onClick={handleRemove}
            disabled={checkedSelectedIds.length === 0}
            leftIcon="trash"
          >
            Quitar ({checkedSelectedIds.length})
          </Button>
        </div>
      </div>
    </div>
  );
};

export default DualTransferList;
