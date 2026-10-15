'use client';

import React, { forwardRef } from 'react';
import { Icon } from '../Icon';

export interface SelectOption {
  value: string | number;
  label: string;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  options: SelectOption[];
  placeholder?: string;
  error?: boolean | string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ options, placeholder, error, className = '', disabled, ...props }, ref) => {
    return (
      <div className="relative w-full">
        <select
          ref={ref}
          disabled={disabled}
          className={`w-full bg-white text-[#000000] text-base px-4 py-2.5 pr-10 rounded-lg border appearance-none outline-none transition-colors cursor-pointer
            ${
              error
                ? 'border-[#D32F2F] focus:border-[#D32F2F] focus:ring-1 focus:ring-[#D32F2F]'
                : 'border-[#3F2581]/50 focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20'
            }
            ${disabled ? 'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed' : ''}
            ${className}`}
          {...props}
        >
          {placeholder && (
            <option value="" disabled className="text-gray-400">
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <div className="absolute right-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-[#3F2581]">
          <Icon name="chevron_down" size={18} />
        </div>
      </div>
    );
  }
);

Select.displayName = 'Select';

export default Select;
