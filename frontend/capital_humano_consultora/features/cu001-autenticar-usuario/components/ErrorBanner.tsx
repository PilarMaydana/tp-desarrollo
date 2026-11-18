'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';

export interface ErrorBannerProps {
  message: string | null;
  onClose?: () => void;
  className?: string;
}

export const ErrorBanner: React.FC<ErrorBannerProps> = ({
  message,
  onClose,
  className = '',
}) => {
  if (!message) return null;

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`w-full p-3 bg-red-50 border border-red-200 text-[#D32F2F] text-sm rounded-xl font-medium flex items-center justify-between gap-3 shadow-xs transition-all duration-200 ${className}`}
    >
      <span className="flex-1 text-center leading-snug">{message}</span>

      {onClose && (
        <button
          type="button"
          onClick={onClose}
          className="shrink-0 text-[#D32F2F]/70 hover:text-[#D32F2F] p-1 rounded-md transition-colors focus:outline-none cursor-pointer"
          aria-label="Cerrar mensaje de error"
        >
          <Icon name="close" size={16} />
        </button>
      )}
    </div>
  );
};

export default ErrorBanner;
