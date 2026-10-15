'use client';

import React, { forwardRef, useState } from 'react';
import { Icon, IconName } from '../Icon';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  leftIcon?: IconName | string;
  rightIcon?: IconName | string;
  error?: boolean | string;
  allowTogglePassword?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      leftIcon,
      rightIcon,
      error,
      type = 'text',
      allowTogglePassword = false,
      className = '',
      disabled,
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPassword = type === 'password' || allowTogglePassword;
    const computedType = isPassword ? (showPassword ? 'text' : 'password') : type;

    return (
      <div className="relative flex items-center w-full">
        {leftIcon && (
          <div className="absolute left-3.5 text-[#3F2581] pointer-events-none flex items-center justify-center">
            <Icon name={leftIcon} size={20} />
          </div>
        )}

        <input
          ref={ref}
          type={computedType}
          disabled={disabled}
          className={`w-full bg-white text-[#000000] text-base placeholder-[#8E8E8E] px-4 py-2.5 rounded-lg border transition-colors outline-none
            ${leftIcon ? 'pl-11' : ''}
            ${rightIcon || isPassword ? 'pr-11' : ''}
            ${
              error
                ? 'border-[#D32F2F] focus:border-[#D32F2F] focus:ring-1 focus:ring-[#D32F2F]'
                : 'border-[#3F2581]/50 focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20'
            }
            ${disabled ? 'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed' : ''}
            ${className}`}
          {...props}
        />

        {isPassword ? (
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="absolute right-3.5 text-[#757575] hover:text-[#3F2581] transition-colors p-1"
            tabIndex={-1}
            aria-label={showPassword ? 'Ocultar contraseña' : 'Ver contraseña'}
          >
            <Icon name={showPassword ? 'visibility_off' : 'visibility'} size={19} />
          </button>
        ) : (
          rightIcon && (
            <div className="absolute right-3.5 text-[#757575] pointer-events-none flex items-center">
              <Icon name={rightIcon} size={19} />
            </div>
          )
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
