'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { usePuestos } from '@/hooks/usePuestos';
import { PuestoSchema, PuestoFormData } from '@/schemas/puesto.schema';
import { puestoService } from '@/services/puestoService';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { DualTransferList, TransferItem } from '@/components/organisms/DualTransferList';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

export default function CrearPuestoPage() {
  const router = useRouter();
  const { empresas, competencias, isLoading } = usePuestos();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<PuestoFormData>({
    resolver: zodResolver(PuestoSchema),
    defaultValues: {
      empresaId: '',
      nombrePuesto: '',
      codigo: '',
      descripcion: '',
      competencias: [],
    },
  });

  const availableItems: TransferItem[] = competencias.map((c) => ({
    id: c.id,
    label: c.nombre,
    subtitle: c.descripcion,
    weight: 5,
  }));

  const onSubmit = async (data: PuestoFormData) => {
    try {
      setSubmitError(null);
      await puestoService.createPuesto({
        empresaId: data.empresaId,
        nombrePuesto: data.nombrePuesto,
        codigo: data.codigo,
        descripcion: data.descripcion,
        competencias: data.competencias,
      });
      router.push(ROUTES.CONSULTOR.PUESTOS);
    } catch (err: unknown) {
      setSubmitError((err as Error).message || 'Error al guardar el nuevo puesto');
    }
  };

  const handleCancel = () => {
    router.push(ROUTES.CONSULTOR.PUESTOS);
  };

  return (
    <div className="flex flex-col gap-8 w-full max-w-6xl mx-auto">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Crear Nuevo Puesto</h1>
        <p className="text-sm text-[#757575] mt-1">
          Complete los datos identificatorios del puesto y defina las competencias ponderadas.
        </p>
      </div>

      {submitError && (
        <div className="p-4 bg-red-50 border border-red-200 text-[#D32F2F] text-sm rounded-xl font-medium">
          {submitError}
        </div>
      )}

      {/* Formulario principal */}
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-8">
        {/* Fila 1: Empresa y Código */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <NotchedField
            label="Empresa"
            required
            error={errors.empresaId?.message}
          >
            <Select
              options={empresas.map((e) => ({ value: e.id, label: e.nombre }))}
              placeholder="Seleccione empresa"
              disabled={isLoading}
              {...register('empresaId')}
              error={!!errors.empresaId}
            />
          </NotchedField>

          <NotchedField
            label="Código de puesto"
            error={errors.codigo?.message}
          >
            <Input
              placeholder="Ej: DEV-102 (opcional)"
              {...register('codigo')}
              error={!!errors.codigo}
            />
          </NotchedField>
        </div>

        {/* Fila 2: Nombre del puesto */}
        <NotchedField
          label="Nombre del puesto"
          required
          error={errors.nombrePuesto?.message}
        >
          <Input
            placeholder="Ej: Desarrollador Backend Java Senior"
            {...register('nombrePuesto')}
            error={!!errors.nombrePuesto}
          />
        </NotchedField>

        {/* Fila 3: Descripción */}
        <NotchedField
          label="Descripción del puesto"
          error={errors.descripcion?.message}
        >
          <Textarea
            rows={3}
            placeholder="Responsabilidades, objetivos del cargo y contexto de trabajo..."
            {...register('descripcion')}
            error={!!errors.descripcion}
          />
        </NotchedField>

        {/* Fila 4: Panel doble de competencias y ponderaciones */}
        <div className="flex flex-col gap-2">
          <label className="text-base font-semibold text-[#3F2581] flex items-center gap-1">
            <span>Asignación de Competencias y Ponderaciones</span>
            <span className="text-[#D32F2F]">*</span>
          </label>
          <p className="text-xs text-[#757575] mb-2">
            Seleccione las competencias requeridas del panel izquierdo y asigne a cada una un puntaje de ponderación entre 0 y 10.
          </p>

          <Controller
            name="competencias"
            control={control}
            render={({ field }) => {
              const currentSelected: TransferItem[] = field.value.map((cp) => {
                const compObj = competencias.find((c) => c.id === cp.competenciaId);
                return {
                  id: cp.competenciaId,
                  label: compObj?.nombre || 'Competencia',
                  subtitle: compObj?.descripcion,
                  weight: cp.ponderacion,
                };
              });

              const handleTransferChange = (newItems: TransferItem[]) => {
                field.onChange(
                  newItems.map((item) => ({
                    competenciaId: item.id,
                    ponderacion: item.weight ?? 5,
                  }))
                );
              };

              return (
                <DualTransferList
                  availableTitle="Competencias disponibles"
                  selectedTitle="Competencias asignadas"
                  availableItems={availableItems}
                  selectedItems={currentSelected}
                  onChange={handleTransferChange}
                  withWeights={true}
                />
              );
            }}
          />

          {errors.competencias?.message && (
            <span className="text-xs text-[#D32F2F] font-semibold mt-1">
              {errors.competencias.message}
            </span>
          )}
        </div>

        {/* Barra de Acciones */}
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
    </div>
  );
}
