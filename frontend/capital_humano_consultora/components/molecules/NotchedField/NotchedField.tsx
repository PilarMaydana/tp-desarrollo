'use client';

import React from 'react';

export interface NotchedFieldProps {
  label: string;
  required?: boolean;
  error?: string;
  helperText?: string;
  children: React.ReactNode;
  className?: string;
}

export const NotchedField: React.FC<NotchedFieldProps> = ({
  label,
  required = false,
  error,
  helperText,
  children,
  className = '',
}) => {
  return (
    <div className={`relative flex flex-col w-full pt-2 ${className}`}>
      {/* Label superpuesta estilo Material Notched Outline */}
      <label className="absolute -top-1 left-4 z-10 px-2 bg-white text-xs md:text-sm font-medium text-[#3F2581] pointer-events-none transition-all flex items-center gap-0.5">
        <span>{label}</span>
        {required && <span className="text-[#D32F2F] font-bold">*</span>}
      </label>

      {/* Contenedor del control (Input, Select, Textarea) */}
      <div className="w-full">{children}</div>

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
