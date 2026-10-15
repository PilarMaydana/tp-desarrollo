'use client';

import React from 'react';
import { UserRole } from '@/types/auth';

export interface SegmentedControlOption {
  role: UserRole;
  label: string;
}

export interface SegmentedControlProps {
  value: UserRole;
  onChange: (value: UserRole) => void;
  className?: string;
}

export const SegmentedControl: React.FC<SegmentedControlProps> = ({
  value,
  onChange,
  className = '',
}) => {
  const options: SegmentedControlOption[] = [
    { role: 'CONSULTOR', label: 'Ingreso consultor' },
    { role: 'CANDIDATO', label: 'Ingreso candidato' },
  ];

  return (
    <div
      className={`flex items-center p-1.5 bg-[#EADDFF] rounded-full border border-[#D0BCFF] shadow-inner select-none ${className}`}
    >
      {options.map((opt) => {
        const isActive = value === opt.role;
        return (
          <button
            key={opt.role}
            type="button"
            onClick={() => onChange(opt.role)}
            className={`flex-1 py-2.5 px-4 rounded-full text-sm md:text-base font-semibold transition-all duration-200 cursor-pointer ${
              isActive
                ? 'bg-[#3F2581] text-white shadow-md'
                : 'text-[#4F378A] hover:bg-[#D9CCF0] active:scale-[0.99]'
            }`}
          >
            {opt.label}
          </button>
        );
      })}
    </div>
  );
};

export default SegmentedControl;
