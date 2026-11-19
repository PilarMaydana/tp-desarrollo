'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select, SelectOption } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import {
  AlertaErrores,
  GrillaOpcionesRespuesta,
} from '@/features/cu021-alta-pregunta';
import { PREGUNTA_LIMITS } from '../schemas/modificarPregunta.schema';
import {
  useModificarPregunta,
  UseModificarPreguntaOptions,
} from '../hooks/useModificarPregunta';
import { ModalExitoModificacion } from './ModalExitoModificacion';

export interface ModificarPreguntaScreenProps extends UseModificarPreguntaOptions {
  titulo?: string;
  subtitulo?: string;
}

/**
 * Normaliza a MAYÚSCULAS el valor de un input/textarea preservando la posición del cursor.
 */
function toUpperPreservingCaret(
  e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
) {
  const { selectionStart, selectionEnd, value } = e.target;
  const upper = value.toUpperCase();
  if (upper !== value) {
    e.target.value = upper;
    if (selectionStart !== null && selectionEnd !== null) {
      e.target.setSelectionRange(selectionStart, selectionEnd);
    }
  }
}

export const ModificarPreguntaScreen: React.FC<ModificarPreguntaScreenProps> = ({
  preguntaId,
  titulo = 'MODIFICAR PREGUNTA',
  subtitulo = 'Modifique los datos de la pregunta y sus opciones de respuesta ponderadas.',
  onSuccessRedirect,
  onCancelRedirect,
}) => {
  const {
    pregunta,
    isLoadingPregunta,
    loadError,
    isNotFoundError,
    handleRetryLoad,
    escalas,
    isLoadingEscalas,
    escalasError,
    escalaSeleccionadaId,
    isLoadingEscalaDetalle,
    handleCambioEscala,
    register,
    handleSubmit,
    errors,
    isSubmitting,
    watchedNombre,
    watchedEnunciado,
    watchedDescripcion,
    watchedOpciones,
    handleChangePonderacionRaw,
    serverError,
    erroresAgrupados,
    rowErrors,
    handleDismissErrores,
    isSuccessModalOpen,
    handleConfirmarExito,
    handleCancelar,
    nombreInputRef,
  } = useModificarPregunta({
    preguntaId,
    onSuccessRedirect,
    onCancelRedirect,
  });

  const { ref: formNombreRef, ...nombreRegister } = register('nombre');
  const enunciadoRegister = register('enunciado');
  const descripcionRegister = register('descripcion');

  const escalaOptions: SelectOption[] = useMemo(() => {
    return escalas.map((escala) => ({
      value: String(escala.id),
      label: escala.descripcion
        ? `${escala.nombre} — ${escala.descripcion}`
        : escala.nombre,
    }));
  }, [escalas]);

  const competenciaTexto = pregunta?.competencia || '-';
  const factorTexto = pregunta?.factor || '-';

  const opcionesRootError =
    errors.opciones?.message || errors.opciones?.root?.message;

  const nombreLen = watchedNombre.trim().length;
  const enunciadoLen = watchedEnunciado.trim().length;
  const descripcionLen = watchedDescripcion.trim().length;

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <button
          type="button"
          onClick={handleCancelar}
          className="hover:text-[#3F2581] transition-colors cursor-pointer"
        >
          Gestión de Preguntas
        </button>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Modificar Pregunta</span>
      </nav>

      {/* Card principal */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-10 md:p-12 flex flex-col gap-7">
        {/* Cabecera */}
        <div className="flex flex-col border-b border-[#EADDFF]/70 pb-5">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581] tracking-tight">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1">{subtitulo}</p>
        </div>

        {/* Estado de carga inicial */}
        {isLoadingPregunta && (
          <div className="flex flex-col items-center justify-center py-16 gap-3 text-center">
            <div className="w-9 h-9 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
            <p className="text-sm font-medium text-[#757575]">
              Cargando datos de la pregunta...
            </p>
          </div>
        )}

        {/* Estado de error al cargar (404 u otro error real del backend/red) */}
        {!isLoadingPregunta && loadError && (
          <div className="flex flex-col gap-6 py-4">
            <AlertaErrores
              titulo={
                isNotFoundError
                  ? 'Pregunta no encontrada'
                  : 'No fue posible cargar la pregunta'
              }
              mensajeGeneral={loadError}
            />

            <div className="flex flex-wrap items-center justify-end gap-3">
              {!isNotFoundError && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleRetryLoad}
                >
                  Reintentar
                </Button>
              )}
              <Button
                type="button"
                variant="primary"
                leftIcon="arrow_back"
                onClick={handleCancelar}
              >
                Volver al listado
              </Button>
            </div>
          </div>
        )}

        {/* Formulario principal precargado */}
        {!isLoadingPregunta && !loadError && pregunta && (
          <>
            {/* Panel agrupado de errores (Flujos 2.A, 2.B, 2.C y validaciones backend) sin tapar campos ni botones */}
            <AlertaErrores
              titulo="No fue posible guardar las modificaciones"
              mensajeGeneral={serverError}
              errores={erroresAgrupados}
              onDismiss={handleDismissErrores}
            />

            <form
              onSubmit={handleSubmit}
              noValidate
              className="flex flex-col gap-7"
            >
              {/* Fila 1: Competencia (solo lectura), Factor (solo lectura) y Nombre de Pregunta (editable) */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <NotchedField
                  label="Competencia"
                  required
                  helperText="Campo de solo lectura"
                >
                  <Input
                    value={competenciaTexto}
                    disabled
                    readOnly
                    aria-label="Competencia (solo lectura)"
                    className="uppercase font-medium text-[#4A4458]"
                  />
                </NotchedField>

                <NotchedField
                  label="Factor"
                  required
                  helperText="Campo de solo lectura"
                >
                  <Input
                    value={factorTexto}
                    disabled
                    readOnly
                    aria-label="Factor (solo lectura)"
                    className="uppercase font-medium text-[#4A4458]"
                  />
                </NotchedField>

                <NotchedField
                  label="Nombre de Pregunta"
                  required
                  error={errors.nombre?.message}
                  helperText={`${nombreLen} / ${PREGUNTA_LIMITS.NOMBRE_MAX} caracteres`}
                >
                  <Input
                    placeholder="EJ: RESOLUCIÓN DE CONFLICTOS INTERPERSONALES"
                    disabled={isSubmitting}
                    style={{ textTransform: 'uppercase' }}
                    className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
                    ref={(el) => {
                      formNombreRef(el);
                      nombreInputRef.current = el;
                    }}
                    {...nombreRegister}
                    onChange={(e) => {
                      toUpperPreservingCaret(e);
                      nombreRegister.onChange(e);
                    }}
                    error={!!errors.nombre}
                  />
                </NotchedField>
              </div>

              {/* Fila 2: Pregunta (enunciado) y Descripción (opcional) */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <NotchedField
                  label="Pregunta"
                  required
                  error={errors.enunciado?.message}
                  helperText={`${enunciadoLen} / ${PREGUNTA_LIMITS.ENUNCIADO_MAX} caracteres`}
                >
                  <Textarea
                    rows={4}
                    placeholder="EJ: ¿CÓMO ACTÚA USTED ANTE DESACUERDOS PERSISTENTES DENTRO DEL EQUIPO DE TRABAJO?"
                    disabled={isSubmitting}
                    style={{ textTransform: 'uppercase' }}
                    className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
                    {...enunciadoRegister}
                    onChange={(e) => {
                      toUpperPreservingCaret(e);
                      enunciadoRegister.onChange(e);
                    }}
                    error={!!errors.enunciado}
                  />
                </NotchedField>

                <NotchedField
                  label="Descripción"
                  error={errors.descripcion?.message}
                  helperText={`Opcional — ${descripcionLen} / ${PREGUNTA_LIMITS.DESCRIPCION_MAX} caracteres`}
                >
                  <Textarea
                    rows={4}
                    placeholder="EJ: EVALÚA CAPACIDAD DE ESCUCHA ACTIVA, MEDIACIÓN Y BÚSQUEDA DE CONSENSO."
                    disabled={isSubmitting}
                    style={{ textTransform: 'uppercase' }}
                    className="uppercase font-normal placeholder:font-normal placeholder:text-gray-400"
                    {...descripcionRegister}
                    onChange={(e) => {
                      toUpperPreservingCaret(e);
                      descripcionRegister.onChange(e);
                    }}
                    error={!!errors.descripcion}
                  />
                </NotchedField>
              </div>

              {/* Fila 3: Selector de Opción de Respuesta (escala) y Grilla de Opciones */}
              <div className="flex flex-col gap-4 pt-2 border-t border-gray-100">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-start">
                  <div>
                    <h2 className="text-base font-bold text-[#3F2581] flex items-center gap-1">
                      <span>Opciones de Respuesta</span>
                      <span className="text-[#D32F2F]">*</span>
                    </h2>
                    <p className="text-xs text-[#757575] mt-0.5">
                      Edite las ponderaciones de las opciones actuales o seleccione una escala para reemplazarlas.
                    </p>
                  </div>

                  <NotchedField
                    label="Opción de Respuesta"
                    error={escalasError ?? undefined}
                    helperText="Elegí una escala solo si querés reemplazar las opciones actuales"
                  >
                    <Select
                      placeholder={
                        isLoadingEscalas
                          ? 'CARGANDO ESCALAS...'
                          : 'SELECCIONAR ESCALA (OPCIONAL)'
                      }
                      options={escalaOptions}
                      value={escalaSeleccionadaId}
                      onChange={(e) => handleCambioEscala(e.target.value)}
                      disabled={
                        isLoadingEscalas ||
                        isLoadingEscalaDetalle ||
                        isSubmitting
                      }
                      error={Boolean(escalasError)}
                    />
                  </NotchedField>
                </div>

                {escalaSeleccionadaId && !isLoadingEscalaDetalle && (
                  <div className="flex items-center gap-2 text-xs text-[#3F2581] bg-[#F4F0FF] border border-[#D0BCFF] rounded-lg px-3.5 py-2">
                    <Icon name="format_list_numbered" size={16} className="shrink-0" />
                    <span>
                      Se reemplazaron las opciones con la escala seleccionada. Complete las ponderaciones para que sumen exactamente 10 puntos.
                    </span>
                  </div>
                )}

                <GrillaOpcionesRespuesta
                  opciones={watchedOpciones}
                  onChangePonderacionRaw={handleChangePonderacionRaw}
                  error={opcionesRootError}
                  rowErrors={rowErrors}
                  disabled={isSubmitting || isLoadingEscalaDetalle}
                  isLoading={isLoadingEscalaDetalle}
                />
              </div>

              {/* Barra de acciones: Cancelar y Aceptar */}
              <ActionToolbar align="right" className="mt-2">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleCancelar}
                  disabled={isSubmitting}
                  className="w-full sm:w-32 font-semibold h-11 px-4 whitespace-nowrap"
                >
                  Cancelar
                </Button>

                <Button
                  type="submit"
                  variant="primary"
                  isLoading={isSubmitting}
                  disabled={isLoadingEscalaDetalle}
                  className="w-full sm:w-32 font-semibold shadow-xs h-11 px-4 whitespace-nowrap"
                >
                  Aceptar
                </Button>
              </ActionToolbar>
            </form>
          </>
        )}
      </div>

      {/* Modal de éxito (200 OK): «La operación ha culminado con éxito» */}
      <ModalExitoModificacion
        isOpen={isSuccessModalOpen}
        onAceptar={handleConfirmarExito}
      />
    </div>
  );
};

export default ModificarPreguntaScreen;
