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
import { competenciaService, ApiError } from '../services/competenciaService';

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
    formState: { errors, isSubmitting },
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
      const response = await competenciaService.crearCompetencia({
        codigo: data.codigo.trim().toUpperCase(),
        nombre: data.nombre.trim().toUpperCase(),
        descripcion: data.descripcion.trim().toUpperCase(),
      });
      setCreatedCompetenciaNombre(response.nombre || data.nombre.trim().toUpperCase());
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as {
          mensaje?: string;
          detalles?: { campo: string; mensaje: string }[];
        } | undefined;

        if (err.status === 409) {
          setServerError(
            errorData?.mensaje || err.message || 'Ya existe una competencia con ese código o nombre'
          );
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

        setServerError(errorData?.mensaje || err.message || 'Error al comunicarse con el servidor');
      } else if (err instanceof Error) {
        setServerError(err.message || 'Error al comunicarse con el servidor');
      } else {
        setServerError('Error al comunicarse con el servidor');
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
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan cohesivas con el resto del sistema */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <button
          type="button"
          onClick={handleCancel}
          className="hover:text-[#3F2581] transition-colors cursor-pointer"
        >
          Gestión de Competencias
        </button>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Dar de alta Competencia</span>
      </nav>

      {/* Card principal con fondo blanco uniforme para los campos NotchedField */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-10 md:p-12 flex flex-col gap-8">
        {/* Cabecera de la pantalla */}
        <div className="flex flex-col">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            Dar de Alta Competencia
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            Ingrese los datos requeridos para registrar una nueva competencia.
          </p>
        </div>

        {/* Flujo Alternativo 2.B: Error de servidor (ej: 409 Conflicto por código o nombre existente) */}
        {serverError && (
          <div
            role="alert"
            className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F]"
          >
            {serverError}
          </div>
        )}

        {/* Flujo Alternativo 2.A: Banner con todas las omisiones juntas (Zod o 400 detalles[]) */}
        {errorMessages.length > 0 && (
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
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-7">
          {/* Fila 1: Código y Nombre */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <NotchedField
              label="Código"
              required
              error={errors.codigo?.message}
              helperText="Máx. 20 caracteres"
            >
              <Input
                placeholder="EJ: TRB-01"
                maxLength={20}
                className="uppercase font-mono font-medium"
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
              helperText="Máx. 100 caracteres"
            >
              <Input
                placeholder="EJ: TRABAJO EN EQUIPO"
                maxLength={100}
                className="uppercase font-medium"
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
            helperText="Máx. 500 caracteres"
          >
            <Textarea
              rows={4}
              placeholder="EJ: CAPACIDAD PARA COLABORAR EFICAZMENTE CON OTRAS PERSONAS."
              maxLength={500}
              className="uppercase"
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
              disabled={isSubmitting}
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
      </div>

      {/* Modal de Confirmación de Éxito */}
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
