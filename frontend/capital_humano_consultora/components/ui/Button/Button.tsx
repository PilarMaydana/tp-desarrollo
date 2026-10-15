'use client';

import React from 'react';
import { Icon, IconName } from '../Icon';

export type ButtonVariant = 'primary' | 'secondary' | 'secondary-soft' | 'icon' | 'outline' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  leftIcon?: IconName | string;
  rightIcon?: IconName | string;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon,
  rightIcon,
  disabled,
  className = '',
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center justify-center font-medium transition-all duration-200 cursor-pointer focus:outline-none select-none disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none';

  const sizeStyles: Record<ButtonSize, string> = {
    sm: 'text-sm py-1.5 px-3 rounded-md gap-1.5',
    md: 'text-base py-2.5 px-6 rounded-lg gap-2',
    lg: 'text-lg py-3 px-8 rounded-xl gap-3 font-semibold',
  };

  const variantStyles: Record<ButtonVariant, string> = {
    primary:
      'bg-[#3F2581] hover:bg-[#4E309D] active:bg-[#341E6C] text-white shadow-sm hover:shadow-md border border-transparent',
    secondary:
      'bg-[#D8D8D8] hover:bg-[#C0C0C0] active:bg-[#B0B0B0] text-[#000000] border border-[#CCCCCC]',
    'secondary-soft':
      'bg-[#CDCEE6] hover:bg-[#B8B9DA] active:bg-[#A6A8CD] text-[#2D1B60] border border-[#BDBEE0]',
    icon:
      'p-2.5 rounded-lg bg-transparent hover:bg-white/10 active:bg-white/20 text-white border border-white/20',
    outline:
      'bg-transparent hover:bg-[#F4F0FF] text-[#3F2581] border-2 border-[#3F2581]',
    danger:
      'bg-[#D32F2F] hover:bg-[#B71C1C] active:bg-[#9A0007] text-white',
  };

  return (
    <button
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? (
        <span className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin mr-2" />
      ) : (
        leftIcon && <Icon name={leftIcon} size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
      )}
      {children}
      {!isLoading && rightIcon && (
        <Icon name={rightIcon} size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
      )}
    </button>
  );
};

export default Button;
