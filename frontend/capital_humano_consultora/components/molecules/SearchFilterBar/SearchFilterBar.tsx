'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';

export interface FilterField {
  key: string;
  label: string;
  placeholder?: string;
  type?: 'text' | 'select';
  options?: { value: string; label: string }[];
}

export interface SearchFilterBarProps {
  fields: FilterField[];
  onSearch: (values: Record<string, string>) => void;
  className?: string;
}

export const SearchFilterBar: React.FC<SearchFilterBarProps> = ({
  fields,
  onSearch,
  className = '',
}) => {
  const [filters, setFilters] = useState<Record<string, string>>({});

  const handleChange = (key: string, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      onSearch(filters);
    }
  };

  return (
    <div
      className={`bg-white p-4 rounded-xl border border-[#D0BCFF]/60 shadow-xs flex flex-col md:flex-row items-stretch md:items-end gap-3 ${className}`}
    >
      <div className="flex-1 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
        {fields.map((field) => (
          <div key={field.key} className="relative pt-1 flex flex-col">
            <label className="text-xs font-medium text-[#3F2581] mb-1 pl-1">
              {field.label}
            </label>
            {field.type === 'select' ? (
              <select
                value={filters[field.key] || ''}
                onChange={(e) => handleChange(field.key, e.target.value)}
                className="w-full bg-[#FAFAFA] text-[#000000] text-sm px-3 py-2 rounded-lg border border-[#BDBEE0] focus:border-[#3F2581] focus:bg-white outline-none"
              >
                <option value="">{field.placeholder || 'Todos'}</option>
                {field.options?.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                placeholder={field.placeholder || ''}
                value={filters[field.key] || ''}
                onChange={(e) => handleChange(field.key, e.target.value)}
                onKeyDown={handleKeyDown}
                className="w-full bg-[#FAFAFA] text-[#000000] text-sm px-3 py-2 rounded-lg border border-[#BDBEE0] focus:border-[#3F2581] focus:bg-white outline-none"
              />
            )}
          </div>
        ))}
      </div>

      <div className="flex items-center gap-2 pt-2 md:pt-0">
        <Button
          type="button"
          variant="primary"
          onClick={() => onSearch(filters)}
          leftIcon="search"
          className="w-full md:w-auto h-[42px] px-5"
        >
          Buscar
        </Button>
      </div>
    </div>
  );
};

export default SearchFilterBar;
