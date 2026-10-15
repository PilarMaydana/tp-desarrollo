'use client';

import React, { forwardRef } from 'react';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  error?: boolean | string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ error, className = '', disabled, rows = 4, ...props }, ref) => {
    return (
      <textarea
        ref={ref}
        rows={rows}
        disabled={disabled}
        className={`w-full bg-white text-[#000000] text-base px-4 py-2.5 rounded-lg border outline-none transition-colors resize-y
          ${
            error
              ? 'border-[#D32F2F] focus:border-[#D32F2F] focus:ring-1 focus:ring-[#D32F2F]'
              : 'border-[#3F2581]/50 focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20'
          }
          ${disabled ? 'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed' : ''}
          ${className}`}
        {...props}
      />
    );
  }
);

Textarea.displayName = 'Textarea';

export default Textarea;
