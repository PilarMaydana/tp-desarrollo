'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select, SelectOption } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Button } from '@/components/ui/Button';
import { GrillaOpcionesRespuesta } from './GrillaOpcionesRespuesta';
import { ModalConfirmacionExito } from './ModalConfirmacionExito';
import { ModalGenerarConIA } from './ModalGenerarConIA';
import { useAltaPregunta, UseAltaPreguntaOptions } from '../hooks/useAltaPregunta';
import { PLANTILLAS_OPCIONES_RESPUESTA } from '../services/altaPreguntaService';

export interface AltaPreguntaScreenProps extends UseAltaPreguntaOptions {
  titulo?: string;
  subtitulo?: string;
}

export const AltaPreguntaScreen: React.FC<AltaPreguntaScreenProps> = ({
  titulo = 'Dar de Alta Pregunta',
  subtitulo = 'Ingrese los datos requeridos para registrar una nueva pregunta asociada a una competencia y factor.',
  onSuccessRedirect,
  onCancelRedirect,
  defaultCompetenciaCodigo,
  defaultFactorCodigo,
}) => {
  const {
    register,
    handleSubmit,
    errors,
    isSubmitting,
    competencias,
    isLoadingCompetencias,
    factores,
    isLoadingFactores,
    watchedCompetenciaCodigo,
    watchedPlantillaOpcionId,
    watchedOpcionesRespuesta,
    handleCambioPlantilla,
    handleChangePonderacion,
    handleUpdateOpcion,
    handleAddOpcion,
    handleRemoveOpcion,
    isSuccessModalOpen,
    preguntaCreadaNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    isIaModalOpen,
    handleOpenIaModal,
    handleCloseIaModal,
    handleApplyIaDraft,
    factorSeleccionadoNombre,
    nombreInputRef,
  } = useAltaPregunta({
    onSuccessRedirect,
    onCancelRedirect,
    defaultCompetenciaCodigo,
    defaultFactorCodigo,
  });

  const { ref: formNombreRef, ...nombreRegister } = register('nombre', {
    onChange: (e) => {
      e.target.value = e.target.value.toUpperCase();
    },
  });

  // Opciones de selector de competencias
  const competenciaOptions: SelectOption[] = useMemo(() => {
    return competencias.map((comp) => ({
      value: comp.codigo,
      label: `${comp.codigo} - ${comp.nombre}`,
    }));
  }, [competencias]);

  // Opciones de selector de factores
  const factorOptions: SelectOption[] = useMemo(() => {
    return factores.map((fac) => ({
      value: fac.codigo,
      label: `${fac.codigo} - ${fac.nombre}`,
    }));
  }, [factores]);

  // Opciones del selector de plantillas de escalas de respuesta (con opciones estándar)
  const plantillaOptions: SelectOption[] = useMemo(() => {
    return PLANTILLAS_OPCIONES_RESPUESTA.map((p) => ({
      value: p.id,
      label: p.nombre,
    }));
  }, []);

  const isCustomScale = watchedPlantillaOpcionId === 'PERSONALIZADA';

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Breadcrumb idéntico al sistema de navegación */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <span>Gestión de Preguntas</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Dar de alta Pregunta</span>
      </nav>

      {/* Card principal */}
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

        {/* Formulario principal */}
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-7">
          {/* Fila 1: Competencia y Factor */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <NotchedField
              label="Competencia"
              required
              error={errors.codigoCompetencia?.message}
              helperText={
                isLoadingCompetencias
                  ? 'Cargando competencias...'
                  : 'Seleccione la competencia a la que pertenece la pregunta'
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
                {...register('codigoCompetencia')}
                error={!!errors.codigoCompetencia}
              />
            </NotchedField>

            <NotchedField
              label="Factor"
              required
              error={errors.codigoFactor?.message}
              helperText={
                isLoadingFactores
                  ? 'Consultando factores...'
                  : 'Seleccione el factor asociado a la pregunta'
              }
            >
              <Select
                placeholder={
                  !watchedCompetenciaCodigo
                    ? 'SELECCIONE PRIMERO UNA COMPETENCIA'
                    : isLoadingFactores
                    ? 'CARGANDO FACTORES...'
                    : 'SELECCIONE UN FACTOR'
                }
                options={factorOptions}
                disabled={!watchedCompetenciaCodigo || isLoadingFactores || isSubmitting}
                {...register('codigoFactor')}
                error={!!errors.codigoFactor}
              />
            </NotchedField>
          </div>

          {/* Fila 2: Nombre de la Pregunta */}
          <NotchedField
            label="Nombre"
            required
            error={errors.nombre?.message}
          >
            <Input
              placeholder="EJ: COMUNICACIÓN ASERTIVA EN RESOLUCIÓN DE CONFLICTOS"
              maxLength={100}
              style={{ textTransform: 'uppercase' }}
              className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
              ref={(e) => {
                formNombreRef(e);
                nombreInputRef.current = e;
              }}
              {...nombreRegister}
              error={!!errors.nombre}
            />
          </NotchedField>

          {/* Fila 3: Pregunta */}
          <NotchedField
            label="Pregunta"
            required
            error={errors.enunciado?.message}
            helperText="Máx. 500 caracteres"
          >
            <Textarea
              rows={3}
              maxLength={500}
              placeholder="EJ: ¿CÓMO COMUNICA SUS IDEAS ANTE SITUACIONES DE CONFLICTO?"
              style={{ textTransform: 'uppercase' }}
              className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
              {...register('enunciado', {
                onChange: (e) => {
                  e.target.value = e.target.value.toUpperCase();
                },
              })}
              error={!!errors.enunciado}
            />
          </NotchedField>

          {/* Fila 4: Descripción (Opcional) */}
          <NotchedField
            label="Descripción"
            error={errors.descripcion?.message}
            helperText="Máx. 500 caracteres (opcional)."
          >
            <Textarea
              rows={2}
              maxLength={500}
              placeholder="EJ: EVALÚA LA CAPACIDAD DE TRANSMITIR CON FIRMEZA Y RESPETO EN CONTEXTOS DE ALTA TENSIÓN."
              style={{ textTransform: 'uppercase' }}
              className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
              {...register('descripcion', {
                onChange: (e) => {
                  e.target.value = e.target.value.toUpperCase();
                },
              })}
              error={!!errors.descripcion}
            />
          </NotchedField>

          {/* Fila 5: Selector de Opción de Respuesta y Grilla editable de Opciones */}
          <div className="flex flex-col gap-3 pt-2">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-center">
              <div>
                <label className="text-base font-bold text-[#3F2581] flex items-center gap-1">
                  <span>Grilla de Opciones de Respuesta</span>
                  <span className="text-[#D32F2F]">*</span>
                </label>
                <p className="text-xs text-[#757575] mt-0.5">
                  Seleccione una escala predefinida o personalice las opciones. Las ponderaciones deben sumar exactamente 10 puntos.
                </p>
              </div>

              {/* Selector de plantilla de respuestas */}
              <NotchedField
                label="Opción de Respuesta"
                required
                error={errors.plantillaOpcionId?.message}
              >
                <Select
                  options={plantillaOptions}
                  value={watchedPlantillaOpcionId}
                  onChange={(e) => handleCambioPlantilla(e.target.value)}
                  disabled={isSubmitting}
                  error={!!errors.plantillaOpcionId}
                />
              </NotchedField>
            </div>

            {/* Grilla editable de opciones - Siempre visible con escala por defecto */}
            {Boolean(watchedPlantillaOpcionId) && (
              <GrillaOpcionesRespuesta
                opciones={watchedOpcionesRespuesta}
                onChangePonderacion={handleChangePonderacion}
                onUpdateOpcion={handleUpdateOpcion}
                onAddOpcion={handleAddOpcion}
                onRemoveOpcion={handleRemoveOpcion}
                isCustomScale={isCustomScale}
                error={errors.opcionesRespuesta?.message}
                disabled={isSubmitting}
              />
            )}
          </div>

          {/* Fila 6: Botones del Formulario con misma altura y texto en un renglón */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-6 border-t border-gray-100 mt-2">
            {/* Botón «Generar con IA» con nowrap */}
            <Button
              type="button"
              variant="secondary-soft"
              onClick={handleOpenIaModal}
              disabled={isSubmitting}
              leftIcon="sparkles"
              size="md"
              className="font-semibold text-[#3F2581] bg-[#F3EDF7] hover:bg-[#E8DEF8] border-[#D0BCFF] whitespace-nowrap px-5 h-11"
            >
              Generar con IA
            </Button>

            {/* Botones «Cancelar» y «Aceptar» más angostos con la misma altura */}
            <div className="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto justify-end">
              <Button
                type="button"
                variant="secondary"
                onClick={handleCancelar}
                disabled={isSubmitting}
                size="md"
                className="w-full sm:w-32 font-semibold h-11 px-4 whitespace-nowrap"
              >
                Cancelar
              </Button>

              <Button
                type="submit"
                variant="primary"
                isLoading={isSubmitting}
                size="md"
                className="w-full sm:w-32 font-semibold shadow-xs h-11 px-4 whitespace-nowrap"
              >
                Aceptar
              </Button>
            </div>
          </div>
        </form>
      </div>

      {/* Modal de Confirmación de Éxito (Paso 3 y 4 del Flujo Principal) */}
      <ModalConfirmacionExito
        isOpen={isSuccessModalOpen}
        preguntaNombre={preguntaCreadaNombre}
        onConfirmarSi={handleCargarOtro}
        onConfirmarNo={handleFinalizar}
      />

      {/* Modal «Generar con IA» (CU 033) */}
      <ModalGenerarConIA
        isOpen={isIaModalOpen}
        onClose={handleCloseIaModal}
        onApplyDraft={handleApplyIaDraft}
        factorNombreSeleccionado={factorSeleccionadoNombre}
      />
    </div>
  );
};

export default AltaPreguntaScreen;
