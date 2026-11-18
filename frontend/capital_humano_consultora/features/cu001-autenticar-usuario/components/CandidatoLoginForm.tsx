'use client';

import React from 'react';
import { UseFormReturn } from 'react-hook-form';
import { CandidatoLoginFormValues } from '../schemas/auth.schema';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';

export interface CandidatoLoginFormProps {
  form: UseFormReturn<CandidatoLoginFormValues>;
  onSubmit: (e?: React.BaseSyntheticEvent) => Promise<void>;
  isLoading?: boolean;
}

export const CandidatoLoginForm: React.FC<CandidatoLoginFormProps> = ({
  form,
  onSubmit,
  isLoading = false,
}) => {
  const {
    register,
    formState: { errors },
  } = form;

  const tipoDocumentoOptions = [
    { value: 'DNI', label: 'Documento Nacional de Identidad (DNI)' },
    { value: 'LE', label: 'Libreta de Enrolamiento (LE)' },
    { value: 'LC', label: 'Libreta Cívica (LC)' },
    { value: 'PP', label: 'Pasaporte (PP)' },
  ];

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-5 w-full" noValidate>
      {/* Campo: Tipo de documento */}
      <NotchedField
        label="Tipo de documento"
        required
        error={errors.tipoDocumento?.message}
      >
        <Select
          id="candidato-tipo-documento"
          options={tipoDocumentoOptions}
          error={!!errors.tipoDocumento}
          {...register('tipoDocumento')}
        />
      </NotchedField>

      {/* Campo: Número de documento */}
      <NotchedField
        label="Número de documento"
        required
        error={errors.numeroDocumento?.message}
      >
        <Input
          id="candidato-numero-documento"
          leftIcon="person"
          placeholder="Sin puntos ni espacios"
          autoComplete="username"
          error={!!errors.numeroDocumento}
          {...register('numeroDocumento')}
        />
      </NotchedField>

      {/* Campo: Contraseña / Clave */}
      <NotchedField
        label="Contraseña"
        required
        error={errors.clave?.message}
      >
        <Input
          id="candidato-clave"
          type="password"
          leftIcon="lock"
          allowTogglePassword
          placeholder="••••••••"
          maxLength={8}
          autoComplete="current-password"
          error={!!errors.clave}
          {...register('clave')}
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

export default CandidatoLoginForm;
