'use client';

import React from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Textarea } from '@/components/ui/Textarea';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import {
  useAltaOpcionRespuesta,
  UseAltaOpcionRespuestaOptions,
} from '../hooks/useAltaOpcionRespuesta';

export type AltaOpcionRespuestaFormProps = UseAltaOpcionRespuestaOptions;

export const AltaOpcionRespuestaForm: React.FC<AltaOpcionRespuestaFormProps> = ({
  onSuccessNavigate,
  onCancelNavigate,
}) => {
  const {
    register,
    rows,
    errors,
    errorMessages,
    isSubmitting,
    serverError,
    createdOpcionNombre,
    handleAddItem,
    handleRemoveItem,
    handleSubmit,
    handleLoadAnother,
    handleFinish,
    handleCancel,
  } = useAltaOpcionRespuesta({ onSuccessNavigate, onCancelNavigate });

  const nombreRegister = register('nombre');
  const descripcionRegister = register('descripcion');

  // Evita que presionar Enter dentro de un campo input envíe el formulario o dispare botones de fila
  const handleInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
    }
  };

  const itemsRootError = errors.items?.message || errors.items?.root?.message;

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan cohesivas con el resto del sistema */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <button
          type="button"
          onClick={handleCancel}
          className="hover:text-[#3F2581] transition-colors cursor-pointer"
        >
          Opciones de Respuesta
        </button>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">
          Dar de alta Opción de Respuesta
        </span>
      </nav>

      {/* Card principal con fondo blanco uniforme para los campos NotchedField */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-10 md:p-12 flex flex-col gap-8">
        {/* Cabecera de la pantalla */}
        <div className="flex flex-col gap-4">
          <div className="flex flex-col">
            <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
              Dar de Alta Opción de Respuesta
            </h1>
            <p className="text-sm text-[#757575] mt-1">
              Crea una escala de valoración para asignar a preguntas y reactivos de evaluación.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-4 border-t border-gray-100">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-[#F4F0FF] border border-[#D0BCFF] flex items-center justify-center text-[#3F2581] shrink-0">
                <Icon name="assignment" size={20} />
              </div>
              <div>
                <h2 className="text-base font-bold text-[#3F2581]">
                  Definición de Escala
                </h2>
                <p className="text-xs text-[#757575]">
                  Ingresa los datos generales y los valores posibles de la escala.
                </p>
              </div>
            </div>
            <span className="text-xs font-medium text-[#757575]">
              <span className="text-[#D32F2F] font-bold">*</span> Campos obligatorios
            </span>
          </div>
        </div>

        {/* Flujos Alternativos 2.B (409 Nombre existente) y 2.C/2.D (422 Regla de negocio) */}
        {serverError && (
          <div
            role="alert"
            className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F]"
          >
            {serverError}
          </div>
        )}

        {/* Flujo Alternativo 2.A: Cuadro superior que lista TODAS las omisiones/errores juntos */}
        {errorMessages.length > 0 && (
          <div
            role="alert"
            className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F] flex flex-col gap-1"
          >
            <span className="font-semibold">
              Por favor, complete o corrija los siguientes datos requeridos:
            </span>
            <ul className="list-disc list-inside space-y-0.5 pl-1">
              {errorMessages.map((msg, idx) => (
                <li key={idx}>{msg}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Formulario principal */}
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-7">
          {/* Campo 1: Nombre */}
          <NotchedField
            label="Nombre"
            required
            error={errors.nombre?.message}
            helperText="Máx. 100 caracteres"
          >
            <Input
              placeholder="EJ: ESCALA DE FRECUENCIA"
              maxLength={100}
              className="uppercase font-medium"
              disabled={isSubmitting}
              {...nombreRegister}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
                nombreRegister.onChange(e);
              }}
              onKeyDown={handleInputKeyDown}
              error={!!errors.nombre}
            />
          </NotchedField>

          {/* Campo 2: Descripción */}
          <NotchedField
            label="Descripción"
            required
            error={errors.descripcion?.message}
            helperText="Máx. 500 caracteres"
          >
            <Textarea
              rows={3}
              placeholder="EJ: ESCALA TIPO LIKERT DE 5 PUNTOS ORIENTADA A MEDIR RECURRENCIA DE CONDUCTAS OPERATIVAS."
              maxLength={500}
              className="uppercase"
              disabled={isSubmitting}
              {...descripcionRegister}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
                descripcionRegister.onChange(e);
              }}
              error={!!errors.descripcion}
            />
          </NotchedField>

          {/* Sección 3: Grilla editable de Opciones */}
          <div className="flex flex-col gap-3 pt-2 border-t border-gray-100">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h3 className="text-sm md:text-base font-bold text-[#3F2581] uppercase tracking-wide flex items-center gap-1">
                  <span>Opciones de la Escala</span>
                  <span className="text-[#D32F2F] font-bold">*</span>
                </h3>
                <p className="text-xs text-[#757575] mt-0.5">
                  Valores ordenados consecutivamente que visualizará el evaluador (mínimo 2 opciones).
                </p>
              </div>

              <Button
                type="button"
                variant="outline"
                size="sm"
                leftIcon="plus"
                onClick={handleAddItem}
                disabled={isSubmitting}
                className="font-semibold text-[#3F2581] bg-[#F4F0FF] hover:bg-[#EADDFF] border border-[#D0BCFF] shadow-2xs whitespace-nowrap px-4 h-10 self-start sm:self-auto"
              >
                Agregar opción
              </Button>
            </div>

            {/* Tabla editable de Opciones (Orden y Descripción) */}
            <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white shadow-xs">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF]">
                      <th className="w-32 sm:w-36 px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-center text-[#3F2581]">
                        Orden <span className="text-[#D32F2F]">*</span>
                      </th>
                      <th className="px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-[#3F2581]">
                        Descripción <span className="text-[#D32F2F]">*</span>
                      </th>
                      <th className="w-20 px-4 py-3.5 text-sm font-bold tracking-wider uppercase text-center text-[#3F2581]">
                        Acción
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#EADDFF]/60 text-sm">
                    {rows.map((row, index) => {
                      const ordenRegister = register(`items.${index}.orden`);
                      const itemDescRegister = register(
                        `items.${index}.descripcion`
                      );
                      const ordenError = errors.items?.[index]?.orden?.message;
                      const descError =
                        errors.items?.[index]?.descripcion?.message;

                      return (
                        <tr
                          key={row.id}
                          className="hover:bg-[#F4F0FF]/50 transition-colors"
                        >
                          {/* Columna Orden (editable) */}
                          <td className="px-4 py-3 align-top">
                            <Input
                              type="number"
                              min={1}
                              step={1}
                              defaultValue={row.defaultOrden}
                              disabled={isSubmitting}
                              aria-label={`Orden de la opción ${index + 1}`}
                              className="text-center font-bold text-[#3F2581] px-2 py-2"
                              {...ordenRegister}
                              onKeyDown={handleInputKeyDown}
                              error={!!ordenError}
                            />
                            {ordenError && (
                              <span className="block text-xs text-[#D32F2F] mt-1 font-medium">
                                {ordenError}
                              </span>
                            )}
                          </td>

                          {/* Columna Descripción (editable, siempre en mayúsculas) */}
                          <td className="px-4 py-3 align-top">
                            <Input
                              type="text"
                              placeholder={`EJ: OPCIÓN ${index + 1}`}
                              maxLength={500}
                              defaultValue={row.defaultDescripcion}
                              disabled={isSubmitting}
                              aria-label={`Descripción de la opción ${index + 1}`}
                              className="uppercase font-normal"
                              {...itemDescRegister}
                              onChange={(e) => {
                                e.target.value = e.target.value.toUpperCase();
                                itemDescRegister.onChange(e);
                              }}
                              onKeyDown={handleInputKeyDown}
                              error={!!descError}
                            />
                            {descError && (
                              <span className="block text-xs text-[#D32F2F] mt-1 font-medium">
                                {descError}
                              </span>
                            )}
                          </td>

                          {/* Columna Acción (quitar fila, deshabilitado si <= 2) */}
                          <td className="px-4 py-3 align-top text-center">
                            <div className="flex items-center justify-center h-[46px]">
                              <button
                                type="button"
                                onClick={() => handleRemoveItem(index)}
                                disabled={isSubmitting || rows.length <= 2}
                                title={
                                  rows.length <= 2
                                    ? 'La opción de respuesta debe tener al menos 2 ítems'
                                    : 'Quitar fila'
                                }
                                aria-label={`Quitar opción ${index + 1}`}
                                className="p-2 text-[#757575] hover:text-[#D32F2F] hover:bg-red-50 rounded-lg transition-colors cursor-pointer disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-transparent disabled:hover:text-[#757575]"
                              >
                                <Icon name="trash" size={18} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            {itemsRootError && (
              <span className="text-xs text-[#D32F2F] mt-1 pl-1 font-medium">
                {itemsRootError}
              </span>
            )}
          </div>

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

      {/* Modal de Confirmación de Éxito (201 Created) */}
      {createdOpcionNombre !== null && (
        <div
          className="fixed inset-0 bg-black/40 z-50 backdrop-blur-xs flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-opcion-respuesta-creada-title"
        >
          <div className="w-full max-w-[440px] bg-white rounded-2xl shadow-2xl border border-[#E0E0E0] p-6 flex flex-col">
            <h2
              id="modal-opcion-respuesta-creada-title"
              className="text-xl font-bold text-[#3F2581]"
            >
              Opción de Respuesta Creada
            </h2>
            <p className="text-sm text-[#333333] mt-2 mb-6">
              La opción{' '}
              <strong className="font-semibold text-[#000000]">
                {createdOpcionNombre}
              </strong>{' '}
              se ha creado correctamente. ¿Desea cargar otra?
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

export default AltaOpcionRespuestaForm;
