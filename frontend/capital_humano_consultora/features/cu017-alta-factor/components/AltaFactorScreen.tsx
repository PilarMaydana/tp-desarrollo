'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select, SelectOption } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Button } from '@/components/ui/Button';
import { ModalConfirmacionExito } from './ModalConfirmacionExito';
import { useAltaFactor, UseAltaFactorOptions } from '../hooks/useAltaFactor';

export interface AltaFactorScreenProps extends UseAltaFactorOptions {
  titulo?: string;
  subtitulo?: string;
}

export const AltaFactorScreen: React.FC<AltaFactorScreenProps> = ({
  titulo = 'Dar de Alta Factor',
  subtitulo = 'Ingrese los datos requeridos para registrar un nuevo factor asociado a una competencia.',
  onSuccessRedirect,
  onCancelRedirect,
  defaultCompetenciaId,
}) => {
  const {
    register,
    handleSubmit,
    errors,
    isSubmitting,
    competencias,
    isLoadingCompetencias,
    factoresCompetencia,
    isLoadingFactores,
    watchedCompetenciaId,
    isSuccessModalOpen,
    factorCreadoNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    codigoInputRef,
  } = useAltaFactor({ onSuccessRedirect, onCancelRedirect, defaultCompetenciaId });

  const { ref: formCodigoRef, ...codigoRegister } = register('codigo', {
    onChange: (e) => {
      e.target.value = e.target.value.toUpperCase();
    },
  });

  // Opciones del selector de competencias
  const competenciaOptions: SelectOption[] = useMemo(() => {
    return competencias.map((comp) => ({
      value: comp.id,
      label: comp.nombre,
    }));
  }, [competencias]);

  // Opciones para el selector de N.º de Orden
  const ordenOptions: SelectOption[] = useMemo(() => {
    if (!watchedCompetenciaId) {
      return [];
    }

    const totalSlots = Math.max(15, factoresCompetencia.length + 5);
    const options: SelectOption[] = [];

    // Mapa de órdenes ocupados con el nombre del factor
    const ocupadosMap = new Map<number, string>();
    factoresCompetencia.forEach((f) => {
      ocupadosMap.set(f.nroOrden, f.nombre);
    });

    for (let i = 1; i <= totalSlots; i++) {
      const factorOcupante = ocupadosMap.get(i);
      if (factorOcupante) {
        options.push({
          value: i,
          label: `${i} - OCUPADO (${factorOcupante})`,
          disabled: true,
        });
      } else {
        options.push({
          value: i,
          label: `${i} - NIVEL DE EVALUACIÓN ${i}`,
        });
      }
    }

    return options;
  }, [watchedCompetenciaId, factoresCompetencia]);

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Breadcrumb idéntico al prototipo de diseño */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <span>Gestión de Factores</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Dar de alta Factor</span>
      </nav>

      {/* Card principal con borde suave y esquinas redondeadas */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-10 md:p-12">
        {/* Título y subtítulo */}
        <div className="flex flex-col mb-8">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            {subtitulo}
          </p>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-7">
          {/* Fila 1: Código y Nombre */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <NotchedField
              label="Código"
              required
              error={errors.codigo?.message}
              helperText="Máx. 50 caracteres"
            >
              <Input
                placeholder="FAC-019"
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

            <NotchedField
              label="Nombre"
              required
              error={errors.nombre?.message}
              helperText="Máx. 100 caracteres"
            >
              <Input
                placeholder="LIDERAZGO ESTRATÉGICO"
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

          {/* Fila 2: Competencia y N.º de Orden */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <NotchedField
              label="Competencia"
              required
              error={errors.competenciaId?.message}
              helperText={
                isLoadingCompetencias
                  ? 'Cargando competencias...'
                  : 'Seleccione la competencia a la que pertenece el factor'
              }
            >
              <Select
                placeholder={
                  isLoadingCompetencias
                    ? 'CARGANDO...'
                    : 'SELECCIONE UNA COMPETENCIA'
                }
                options={competenciaOptions}
                disabled={isLoadingCompetencias || isSubmitting}
                {...register('competenciaId')}
                error={!!errors.competenciaId}
              />
            </NotchedField>

            <NotchedField
              label="N.º de Orden"
              required
              error={errors.nroOrden?.message}
              helperText={
                isLoadingFactores
                  ? 'Consultando órdenes...'
                  : !watchedCompetenciaId
                  ? 'Seleccione primero una competencia'
                  : 'Entero positivo. Único dentro de la competencia seleccionada'
              }
            >
              <Select
                placeholder={
                  !watchedCompetenciaId
                    ? 'SELECCIONE UNA COMPETENCIA PRIMERO'
                    : 'SELECCIONE UN N.º DE ORDEN'
                }
                options={ordenOptions}
                disabled={!watchedCompetenciaId || isSubmitting}
                {...register('nroOrden')}
                error={!!errors.nroOrden}
              />
            </NotchedField>
          </div>

          {/* Fila 3: Descripción */}
          <NotchedField
            label="Descripción"
            required
            error={errors.descripcion?.message}
            helperText="Máx. 500 caracteres"
          >
            <Textarea
              rows={4}
              maxLength={500}
              placeholder="CAPACIDAD PARA DIRIGIR GRUPOS DE ALTO RENDIMIENTO, ESTABLECER METAS CLARAS Y PROMOVER EL DESARROLLO INTEGRAL DE LOS INTEGRANTES DEL EQUIPO ORIENTADO AL CUMPLIMIENTO DE OBJETIVOS ESTRATÉGICOS."
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

          {/* Fila 4: Botones Cancelar y Aceptar alineados a la derecha */}
          <div className="flex items-center justify-end gap-4 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={handleCancelar}
              disabled={isSubmitting}
              className="px-8 min-w-[120px]"
            >
              Cancelar
            </Button>

            <Button
              type="submit"
              variant="primary"
              isLoading={isSubmitting}
              className="px-8 min-w-[120px]"
            >
              Aceptar
            </Button>
          </div>
        </form>
      </div>

      {/* Modal de Confirmación de Éxito */}
      <ModalConfirmacionExito
        isOpen={isSuccessModalOpen}
        factorNombre={factorCreadoNombre}
        onConfirmarSi={handleCargarOtro}
        onConfirmarNo={handleFinalizar}
      />
    </div>
  );
};

export default AltaFactorScreen;
