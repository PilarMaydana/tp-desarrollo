'use client';

import React from 'react';

export interface NotchedFieldProps {
  label: string;
  required?: boolean;
  error?: string;
  helperText?: string;
  children: React.ReactNode;
  className?: string;
  labelBgClassName?: string;
}

export const NotchedField: React.FC<NotchedFieldProps> = ({
  label,
  required = false,
  error,
  helperText,
  children,
  className = '',
  labelBgClassName = 'bg-white',
}) => {
  return (
    <div className={`relative flex flex-col w-full pt-2 ${className}`}>
      {/* Contenedor del control (Input, Select, Textarea) con label centrada en el borde superior */}
      <div className="relative w-full">
        <label
          className={`absolute top-0 -translate-y-1/2 left-3.5 z-10 px-1.5 py-0 leading-none ${labelBgClassName} text-xs md:text-sm font-medium text-[#3F2581] pointer-events-none transition-all flex items-center gap-0.5`}
        >
          <span>{label}</span>
          {required && <span className="text-[#D32F2F] font-bold">*</span>}
        </label>
        {children}
      </div>

      {/* Error / Helper text */}
      {error ? (
        <span className="text-xs text-[#D32F2F] mt-1 pl-1 font-medium">{error}</span>
      ) : helperText ? (
        <span className="text-xs text-[#757575] mt-1 pl-1">{helperText}</span>
      ) : null}
    </div>
  );
};

export default NotchedField;
