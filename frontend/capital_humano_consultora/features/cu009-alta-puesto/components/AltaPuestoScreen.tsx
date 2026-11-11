'use client';

import React from 'react';
import { Controller, FieldErrors } from 'react-hook-form';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Textarea } from '@/components/ui/Textarea';
import { DualTransferList, TransferItem } from '@/components/organisms/DualTransferList';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { ModalConfirmacionExito } from './ModalConfirmacionExito';
import { useAltaPuesto, UseAltaPuestoOptions } from '../hooks/useAltaPuesto';
import { AltaPuestoFormData } from '../schemas/altaPuesto.schema';

export interface AltaPuestoScreenProps extends UseAltaPuestoOptions {
  consultorNombre?: string;
  titulo?: string;
  subtitulo?: string;
}

const EMPRESAS_SUGERIDAS = [
  'TECH SOLUTIONS',
  'BANCO METROPOLITANO',
  'CONSULTORES ASOCIADOS',
  'LOGÍSTICA GLOBAL',
  'GRUPO FINANCIERO SUR',
];

export const AltaPuestoScreen: React.FC<AltaPuestoScreenProps> = ({
  titulo = 'Crear Nuevo Puesto',
  subtitulo = 'Complete los datos identificatorios del puesto y defina las competencias ponderadas.',
  onSuccessRedirect,
  onCancelRedirect,
}) => {
  const {
    register,
    handleSubmit,
    control,
    errors,
    isSubmitting,
    competenciasDisponibles,
    isLoadingCompetencias,
    isSuccessModalOpen,
    puestoCreadoNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    codigoInputRef,
  } = useAltaPuesto({ onSuccessRedirect, onCancelRedirect });

  const { ref: formCodigoRef, ...codigoRegister } = register('codigo', {
    onChange: (e) => {
      e.target.value = e.target.value.toUpperCase();
    },
  });

  const availableTransferItems: TransferItem[] = competenciasDisponibles.map((c) => ({
    id: String(c.id),
    label: c.nombre,
    subtitle: c.descripcion,
  }));

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan idénticas al sistema de navegación */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <span>Gestión de Puestos</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">{titulo}</span>
      </nav>

      {/* Card principal con borde suave y fondo blanco */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-10 md:p-12">
        {/* Título de la pantalla */}
        <div className="flex flex-col mb-8">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            {subtitulo}
          </p>
        </div>

        {/* Formulario Principal (Paso 1 y 2) */}
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-7">
        {/* Fila 1: Código y Nombre del puesto */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-1">
            <NotchedField
              label="Código"
              required
              error={errors.codigo?.message}
              helperText="Máx. 50 caracteres"
            >
              <Input
                placeholder="EJ: DEV-01"
                maxLength={50}
                style={{ textTransform: 'uppercase' }}
                className="uppercase font-mono font-medium"
                ref={(e) => {
                  formCodigoRef(e);
                  codigoInputRef.current = e;
                }}
                {...codigoRegister}
                error={!!errors.codigo}
              />
            </NotchedField>
          </div>

          <div className="md:col-span-2">
            <NotchedField
              label="Nombre del puesto"
              required
              error={errors.nombre?.message}
              helperText="Máx. 100 caracteres"
            >
              <Input
                placeholder="EJ: DESARROLLADOR FULL STACK SENIOR"
                maxLength={100}
                style={{ textTransform: 'uppercase' }}
                className="uppercase font-medium"
                {...register('nombre', {
                  onChange: (e) => {
                    e.target.value = e.target.value.toUpperCase();
                  },
                })}
                error={!!errors.nombre}
              />
            </NotchedField>
          </div>
        </div>

        {/* Fila 2: Empresa */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <NotchedField
            label="Empresa"
            required
            error={errors.empresa?.message}
            helperText="Máx. 100 caracteres. Seleccione una sugerencia o escriba el nombre."
          >
            <div className="relative w-full">
              <Input
                list="lista-empresas-sugeridas"
                placeholder="EJ: TECH SOLUTIONS"
                maxLength={100}
                style={{ textTransform: 'uppercase' }}
                className="uppercase font-medium"
                {...register('empresa', {
                  onChange: (e) => {
                    e.target.value = e.target.value.toUpperCase();
                  },
                })}
                error={!!errors.empresa}
              />
              <datalist id="lista-empresas-sugeridas">
                {EMPRESAS_SUGERIDAS.map((emp) => (
                  <option key={emp} value={emp} />
                ))}
              </datalist>
            </div>
          </NotchedField>
        </div>

        {/* Fila 3: Descripción multilínea */}
        <NotchedField
          label="Descripción"
          required
          error={errors.descripcion?.message}
          helperText="Máx. 500 caracteres"
        >
          <Textarea
            rows={3}
            maxLength={500}
            placeholder="EJ: RESPONSABLE DE LA ARQUITECTURA, DESARROLLO Y MANTENIMIENTO DE SERVICIOS CRÍTICOS..."
            style={{ textTransform: 'uppercase' }}
            className="uppercase"
            {...register('descripcion', {
              onChange: (e) => {
                e.target.value = e.target.value.toUpperCase();
              },
            })}
            error={!!errors.descripcion}
          />
        </NotchedField>

        {/* Fila 4: Características del puesto (Competencias y Ponderaciones) */}
        <div className="flex flex-col gap-2 pt-2">
          <div className="flex items-center justify-between">
            <label className="text-base font-bold text-[#3F2581] flex items-center gap-1">
              <span>Características del puesto (Competencias)</span>
              <span className="text-[#D32F2F]">*</span>
              {isLoadingCompetencias && (
                <span className="text-xs font-normal text-purple-600 animate-pulse ml-2">
                  (Cargando competencias...)
                </span>
              )}
            </label>
            <span className="text-xs text-[#757575]">
              Mínimo 1 competencia requerida. Sin duplicados.
            </span>
          </div>

          <p className="text-xs text-[#757575] mb-2">
            Seleccione las competencias en el panel izquierdo y use el botón «Agregar». Asigne a cada una una ponderación numérica entre 0 y 10 (permite decimales, ej. 8.5).
          </p>

          <Controller
            name="caracteristicas"
            control={control}
            render={({ field }) => {
              const currentSelected: TransferItem[] = (field.value || []).map((cp) => {
                const comp = competenciasDisponibles.find(
                  (c) => String(c.id) === String(cp.competenciaId)
                );
                return {
                  id: String(cp.competenciaId),
                  label: comp?.nombre || cp.nombre || `Competencia ${cp.competenciaId}`,
                  subtitle: comp?.descripcion,
                  weight: cp.ponderacion,
                };
              });

              const handleTransferChange = (newSelectedItems: TransferItem[]) => {
                field.onChange(
                  newSelectedItems.map((item) => ({
                    competenciaId: item.id,
                    nombre: item.label,
                    ponderacion: item.weight,
                  }))
                );
              };

              return (
                <DualTransferList
                  availableTitle="Competencias disponibles"
                  selectedTitle="Competencias asignadas al puesto"
                  availableItems={availableTransferItems}
                  selectedItems={currentSelected}
                  onChange={handleTransferChange}
                  withWeights={true}
                />
              );
            }}
          />

          {errors.caracteristicas && (
            <span className="text-xs text-[#D32F2F] font-semibold mt-1">
              {errors.caracteristicas.message ||
                (Array.isArray(errors.caracteristicas) &&
                  (errors.caracteristicas as (FieldErrors<AltaPuestoFormData['caracteristicas'][number]> | undefined)[]).some(
                    (c) => c?.ponderacion?.message
                  ) &&
                  'Debe ingresar una ponderación válida (entre 0 y 10) para cada competencia')}
            </span>
          )}
        </div>

        {/* Barra de Acciones: Botones «Cancelar» y «Aceptar» */}
        <ActionToolbar align="right">
          <Button
            type="button"
            variant="secondary"
            onClick={handleCancelar}
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

      {/* Modal de Confirmación de Éxito (Paso 3 y 4 del Flujo Principal) */}
      <ModalConfirmacionExito
        isOpen={isSuccessModalOpen}
        puestoNombre={puestoCreadoNombre}
        onConfirmarSi={handleCargarOtro}
        onConfirmarNo={handleFinalizar}
      />
    </div>
  );
};

export default AltaPuestoScreen;
