'use client';

import React from 'react';
import { UseFormReturn } from 'react-hook-form';
import { ConsultorLoginFormValues } from '../schemas/auth.schema';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';

export interface ConsultorLoginFormProps {
  form: UseFormReturn<ConsultorLoginFormValues>;
  onSubmit: (e?: React.BaseSyntheticEvent) => Promise<void>;
  isLoading?: boolean;
}

export const ConsultorLoginForm: React.FC<ConsultorLoginFormProps> = ({
  form,
  onSubmit,
  isLoading = false,
}) => {
  const {
    register,
    formState: { errors },
    setValue,
  } = form;

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-5 w-full" noValidate>
      {/* Campo: Nombre de usuario */}
      <NotchedField
        label="Nombre de usuario"
        required
        error={errors.usuario?.message}
      >
        <Input
          id="consultor-usuario"
          leftIcon="alternate_email"
          placeholder="Ej. lfernandez"
          maxLength={15}
          autoComplete="username"
          autoCapitalize="characters"
          className="uppercase tracking-wide"
          error={!!errors.usuario}
          {...register('usuario', {
            onChange: (e: React.ChangeEvent<HTMLInputElement>) => {
              setValue('usuario', e.target.value.toUpperCase(), {
                shouldValidate: true,
              });
            },
          })}
        />
      </NotchedField>

      {/* Campo: Contraseña */}
      <NotchedField
        label="Contraseña"
        required
        error={errors.contrasena?.message}
      >
        <Input
          id="consultor-contrasena"
          type="password"
          leftIcon="lock"
          allowTogglePassword
          placeholder="••••••••"
          maxLength={20}
          autoComplete="current-password"
          error={!!errors.contrasena}
          {...register('contrasena')}
        />
      </NotchedField>

      {/* Botón Ingresar */}
      <Button
        type="submit"
        variant="primary"
        size="lg"
        isLoading={isLoading}
        className="mt-2 w-full uppercase font-bold tracking-wider"
      >
        INGRESAR
      </Button>
    </form>
  );
};

export default ConsultorLoginForm;
