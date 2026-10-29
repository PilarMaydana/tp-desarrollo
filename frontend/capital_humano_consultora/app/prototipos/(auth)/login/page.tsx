'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { UserRole } from '@/types/auth';
import {
  LoginConsultorSchema,
  LoginCandidatoSchema,
  LoginConsultorFormData,
  LoginCandidatoFormData,
} from '@/schemas/auth.schema';
import { useAuth } from '@/context/AuthContext';
import { AuthLayout } from '@/components/layouts/AuthLayout';
import { SegmentedControl } from '@/components/molecules/SegmentedControl';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

export default function LoginPage() {
  const [activeRole, setActiveRole] = useState<UserRole>('CONSULTOR');
  const { login } = useAuth();
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  // Formulario Consultor
  const consultorForm = useForm<LoginConsultorFormData>({
    resolver: zodResolver(LoginConsultorSchema),
    defaultValues: {
      username: '',
      clave: '',
    },
  });

  // Formulario Candidato
  const candidatoForm = useForm<LoginCandidatoFormData>({
    resolver: zodResolver(LoginCandidatoSchema),
    defaultValues: {
      tipoDocumento: 'DNI',
      nroDocumento: '',
      clave: '',
    },
  });

  const onSubmitConsultor = async (data: LoginConsultorFormData) => {
    try {
      setFormError(null);
      await login({
        username: data.username,
        clave: data.clave,
        role: 'CONSULTOR',
      });
      router.push(ROUTES.CONSULTOR.PUESTOS);
    } catch (err: unknown) {
      setFormError((err as Error).message || 'Credenciales inválidas');
    }
  };

  const onSubmitCandidato = async (data: LoginCandidatoFormData) => {
    try {
      setFormError(null);
      await login({
        username: data.nroDocumento,
        clave: data.clave,
        role: 'CANDIDATO',
        tipoDocumento: data.tipoDocumento,
        nroDocumento: data.nroDocumento,
      });
      router.push(ROUTES.CANDIDATO.CUESTIONARIO);
    } catch (err: unknown) {
      setFormError((err as Error).message || 'Credenciales inválidas');
    }
  };

  return (
    <AuthLayout>
      <div className="flex flex-col gap-6 w-full">
        {/* Selector de Rol */}
        <SegmentedControl value={activeRole} onChange={setActiveRole} />

        {formError && (
          <div className="p-3 bg-red-50 border border-red-200 text-[#D32F2F] text-sm rounded-lg text-center font-medium">
            {formError}
          </div>
        )}

        {/* Formulario Consultor */}
        {activeRole === 'CONSULTOR' ? (
          <form
            onSubmit={consultorForm.handleSubmit(onSubmitConsultor)}
            className="flex flex-col gap-4"
          >
            <NotchedField
              label="Nombre de usuario"
              required
              error={consultorForm.formState.errors.username?.message}
            >
              <Input
                leftIcon="alternate_email"
                placeholder="Ej. lfernandez"
                {...consultorForm.register('username')}
                error={!!consultorForm.formState.errors.username}
              />
            </NotchedField>

            <NotchedField
              label="Contraseña"
              required
              error={consultorForm.formState.errors.clave?.message}
            >
              <Input
                type="password"
                leftIcon="lock"
                allowTogglePassword
                placeholder="••••••••"
                {...consultorForm.register('clave')}
                error={!!consultorForm.formState.errors.clave}
              />
            </NotchedField>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              isLoading={consultorForm.formState.isSubmitting}
              className="mt-4 w-full uppercase tracking-wider"
            >
              Ingresar
            </Button>
          </form>
        ) : (
          /* Formulario Candidato */
          <form
            onSubmit={candidatoForm.handleSubmit(onSubmitCandidato)}
            className="flex flex-col gap-4"
          >
            <NotchedField
              label="Tipo de documento"
              required
              error={candidatoForm.formState.errors.tipoDocumento?.message}
            >
              <Select
                options={[
                  { value: 'DNI', label: 'Documento Nacional de Identidad (DNI)' },
                  { value: 'PAS', label: 'Pasaporte' },
                  { value: 'LC', label: 'Libreta Cívica' },
                ]}
                {...candidatoForm.register('tipoDocumento')}
                error={!!candidatoForm.formState.errors.tipoDocumento}
              />
            </NotchedField>

            <NotchedField
              label="Número de documento"
              required
              error={candidatoForm.formState.errors.nroDocumento?.message}
            >
              <Input
                leftIcon="person"
                placeholder="Sin puntos ni espacios"
                {...candidatoForm.register('nroDocumento')}
                error={!!candidatoForm.formState.errors.nroDocumento}
              />
            </NotchedField>

            <NotchedField
              label="Contraseña"
              required
              error={candidatoForm.formState.errors.clave?.message}
            >
              <Input
                type="password"
                leftIcon="lock"
                allowTogglePassword
                placeholder="••••••••"
                {...candidatoForm.register('clave')}
                error={!!candidatoForm.formState.errors.clave}
              />
            </NotchedField>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              isLoading={candidatoForm.formState.isSubmitting}
              className="mt-4 w-full uppercase tracking-wider"
            >
              Ingresar
            </Button>
          </form>
        )}
      </div>
    </AuthLayout>
  );
}
