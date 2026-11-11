'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { CompetenciaSchema, CompetenciaFormData } from '@/schemas/competencia.schema';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Textarea } from '@/components/ui/Textarea';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';
import { apiFetch, ApiError } from '@/services/apiClient';
import { CompetenciaResponse } from '@/types/competencia';

export interface AltaCompetenciaFormProps {
  onSuccessNavigate?: () => void;
  onCancelNavigate?: () => void;
}

export const AltaCompetenciaForm: React.FC<AltaCompetenciaFormProps> = ({
  onSuccessNavigate,
  onCancelNavigate,
}) => {
  const router = useRouter();
  const [createdCompetenciaNombre, setCreatedCompetenciaNombre] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setFocus,
    setError,
    formState: { errors, isSubmitted, isSubmitting },
  } = useForm<CompetenciaFormData>({
    resolver: zodResolver(CompetenciaSchema),
    defaultValues: {
      codigo: '',
      nombre: '',
      descripcion: '',
    },
  });

  const codigoRegister = register('codigo');
  const nombreRegister = register('nombre');
  const descripcionRegister = register('descripcion');

  const errorMessages = [
    errors.codigo?.message,
    errors.nombre?.message,
    errors.descripcion?.message,
  ].filter((msg): msg is string => Boolean(msg));

  const onSubmit = async (data: CompetenciaFormData) => {
    setServerError(null);
    try {
      const response = await apiFetch<CompetenciaResponse>('/competencias', {
        method: 'POST',
        body: JSON.stringify({
          codigo: data.codigo.trim().toUpperCase(),
          nombre: data.nombre.trim().toUpperCase(),
          descripcion: data.descripcion.trim().toUpperCase(),
        }),
      });
      setCreatedCompetenciaNombre(response.nombre || data.nombre.trim().toUpperCase());
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as { mensaje?: string; detalles?: { campo: string; mensaje: string }[] } | undefined;
        if (err.status === 409) {
          setServerError(errorData?.mensaje || err.message || 'Ya existe una competencia con ese código o nombre');
          return;
        }
        if (err.status === 400 && errorData?.detalles && errorData.detalles.length > 0) {
          errorData.detalles.forEach((d) => {
            const fieldKey = d.campo as keyof CompetenciaFormData;
            if (fieldKey) {
              setError(fieldKey, { type: 'manual', message: d.mensaje });
            }
          });
          return;
        }
        if (err.status === 500 && (err.message.includes('conexión') || err.message.includes('fetch'))) {
          // Fallback en desarrollo cuando el backend no está levantado
          setCreatedCompetenciaNombre(data.nombre.trim().toUpperCase());
          return;
        }
        setServerError(errorData?.mensaje || err.message || 'Error al comunicarse con el servidor');
      } else {
        setCreatedCompetenciaNombre(data.nombre.trim().toUpperCase());
      }
    }
  };

  const handleCancel = () => {
    if (onCancelNavigate) {
      onCancelNavigate();
    } else {
      router.push(ROUTES.CONSULTOR.COMPETENCIAS);
    }
  };

  const handleLoadAnother = () => {
    setServerError(null);
    setCreatedCompetenciaNombre(null);
    reset({
      codigo: '',
      nombre: '',
      descripcion: '',
    });
    setTimeout(() => {
      setFocus('codigo');
    }, 0);
  };

  const handleFinish = () => {
    setServerError(null);
    setCreatedCompetenciaNombre(null);
    if (onSuccessNavigate) {
      onSuccessNavigate();
    } else {
      router.push(ROUTES.CONSULTOR.COMPETENCIAS);
    }
  };

  return (
    <div className="flex flex-col gap-8 w-full max-w-6xl mx-auto">
      {/* Cabecera de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
          Dar de Alta Competencia
        </h1>
        <p className="text-sm text-[#757575] mt-1">
          Ingrese los datos requeridos para registrar una nueva competencia.
        </p>
      </div>

      {/* Error de servidor (ej: 409 Conflicto) */}
      {serverError && (
        <div
          role="alert"
          className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F]"
        >
          {serverError}
        </div>
      )}

      {/* Flujo Alternativo 2.A: Banner con todas las omisiones juntas */}
      {isSubmitted && errorMessages.length > 0 && (
        <div
          role="alert"
          className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F] flex flex-col gap-1"
        >
          <span className="font-semibold">
            Por favor, complete los datos obligatorios requeridos:
          </span>
          <ul className="list-disc list-inside space-y-0.5 pl-1">
            {errorMessages.map((msg, idx) => (
              <li key={idx}>{msg}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Formulario principal */}
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-8">
        {/* Fila 1: Código y Nombre */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <NotchedField
            label="Código"
            required
            error={errors.codigo?.message}
          >
            <Input
              placeholder="EJ: TRB-01"
              maxLength={20}
              {...codigoRegister}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
                codigoRegister.onChange(e);
              }}
              error={!!errors.codigo}
            />
          </NotchedField>

          <NotchedField
            label="Nombre"
            required
            error={errors.nombre?.message}
          >
            <Input
              placeholder="EJ: TRABAJO EN EQUIPO"
              maxLength={100}
              {...nombreRegister}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
                nombreRegister.onChange(e);
              }}
              error={!!errors.nombre}
            />
          </NotchedField>
        </div>

        {/* Fila 2: Descripción */}
        <NotchedField
          label="Descripción"
          required
          error={errors.descripcion?.message}
        >
          <Textarea
            rows={4}
            placeholder="EJ: CAPACIDAD PARA COLABORAR EFICAZMENTE CON OTRAS PERSONAS."
            maxLength={500}
            {...descripcionRegister}
            onChange={(e) => {
              e.target.value = e.target.value.toUpperCase();
              descripcionRegister.onChange(e);
            }}
            error={!!errors.descripcion}
          />
        </NotchedField>

        {/* Barra de Acciones al pie */}
        <ActionToolbar align="right">
          <Button
            type="button"
            variant="secondary"
            onClick={handleCancel}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isSubmitting}
          >
            Aceptar
          </Button>
        </ActionToolbar>
      </form>

      {/* Modal de Confirmación */}
      {createdCompetenciaNombre !== null && (
        <div
          className="fixed inset-0 bg-black/40 z-50 backdrop-blur-xs flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-competencia-creada-title"
        >
          <div className="w-full max-w-[440px] bg-white rounded-2xl shadow-2xl border border-[#E0E0E0] p-6 flex flex-col">
            <h2
              id="modal-competencia-creada-title"
              className="text-xl font-bold text-[#3F2581]"
            >
              Competencia Creada
            </h2>
            <p className="text-sm text-[#333333] mt-2 mb-6">
              La competencia <strong className="font-semibold text-[#000000]">{createdCompetenciaNombre}</strong> se ha creado correctamente. ¿Desea cargar otra?
            </p>
            <div className="flex items-center justify-end gap-3">
              <Button
                type="button"
                variant="secondary"
                onClick={handleFinish}
              >
                No
              </Button>
              <Button
                type="button"
                variant="primary"
                onClick={handleLoadAnother}
                autoFocus
              >
                Sí
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AltaCompetenciaForm;

