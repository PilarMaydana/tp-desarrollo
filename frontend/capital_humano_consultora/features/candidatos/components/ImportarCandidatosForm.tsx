'use client';

import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { ModalResultadoImportacion } from './ModalResultadoImportacion';
import { importarCandidatos } from '@/services/candidatoService';
import {
  ErrorImportacionResponse,
  ImportacionCandidatosResponse,
} from '@/types/candidato';
import { ROUTES } from '@/constants/routes';

export interface ImportarCandidatosFormProps {
  titulo?: string;
  subtitulo?: string;
  onSuccessRedirect?: string;
  onCancelRedirect?: string;
}

const ENCABEZADOS_REQUERIDOS =
  'Nro candidato;Apellido;Nombre;Nro documento;Tipo documento;Fecha de nacimiento;Género;Nacionalidad;Email;Escolaridad;Eliminado';

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

export const ImportarCandidatosForm: React.FC<ImportarCandidatosFormProps> = ({
  titulo = 'Importar Candidatos',
  subtitulo = 'Cargue un archivo CSV para crear, actualizar o dar de baja candidatos de forma masiva.',
  onSuccessRedirect = ROUTES.HOME,
  onCancelRedirect = ROUTES.HOME,
}) => {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorResponse, setErrorResponse] = useState<ErrorImportacionResponse | null>(null);
  const [resultadoExitoso, setResultadoExitoso] = useState<ImportacionCandidatosResponse | null>(null);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);

  // Estados de interfaz interactiva
  const [isCopied, setIsCopied] = useState(false);
  const [isFormatAccordionOpen, setIsFormatAccordionOpen] = useState(false);
  const [isErrorDetailsOpen, setIsErrorDetailsOpen] = useState(false);

  // Formato legible para el tamaño de archivo
  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  // Validar si el archivo es .csv y tamaño máximo
  const validarYAsignarArchivo = (file: File) => {
    setErrorResponse(null);

    if (!file.name.toLowerCase().endsWith('.csv')) {
      setErrorResponse({
        mensaje: 'Extensión de archivo inválida.',
        errores: [
          {
            fila: null,
            campo: null,
            mensaje: 'El archivo seleccionado no tiene la extensión requerida (.csv).',
          },
        ],
      });
      setSelectedFile(null);
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setErrorResponse({
        mensaje: 'El archivo excede el tamaño máximo permitido.',
        errores: [
          {
            fila: null,
            campo: null,
            mensaje: `El tamaño del archivo (${formatFileSize(file.size)}) supera el límite de 5 MB.`,
          },
        ],
      });
      setSelectedFile(null);
      return;
    }

    setSelectedFile(file);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      validarYAsignarArchivo(files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isSubmitting) {
      setIsDragging(true);
    }
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (isSubmitting) return;

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      validarYAsignarArchivo(files[0]);
    }
  };

  const handleRemoveFile = (e?: React.MouseEvent) => {
    if (e) {
      e.stopPropagation();
    }
    if (isSubmitting) return;
    setSelectedFile(null);
    setErrorResponse(null);
    setIsErrorDetailsOpen(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleImportar = async () => {
    if (!selectedFile || isSubmitting) return;

    setIsSubmitting(true);
    setErrorResponse(null);

    try {
      const data = await importarCandidatos(selectedFile);
      setResultadoExitoso(data);
      setIsSuccessModalOpen(true);
    } catch (err: unknown) {
      const errorData = err as ErrorImportacionResponse;
      if (errorData && typeof errorData === 'object' && 'mensaje' in errorData) {
        setErrorResponse(errorData);
      } else {
        setErrorResponse({
          mensaje: 'Ocurrió un error inesperado al procesar la importación.',
          errores: [],
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancelar = () => {
    router.push(onCancelRedirect);
  };

  const handleModalAceptar = () => {
    setIsSuccessModalOpen(false);
    router.push(onSuccessRedirect);
  };

  const handleCopiarEncabezados = async () => {
    try {
      await navigator.clipboard.writeText(ENCABEZADOS_REQUERIDOS);
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    } catch {
      // Fallback si no está disponible la API de portapapeles
      setIsCopied(false);
    }
  };

  return (
    <div className="flex flex-col gap-4 w-full max-w-5xl mx-auto pb-12">
      {/* Migas de pan: Inicio > Importar Candidatos */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <span>Inicio</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">{titulo}</span>
      </nav>

      {/* Tarjeta principal con borde gris sutil y sombra suave */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-gray-200/90 shadow-sm p-6 sm:p-10 md:p-12">
        {/* Encabezado */}
        <div className="flex flex-col mb-8">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1.5 leading-relaxed">
            {subtitulo}
          </p>
        </div>

        {/* Alerta de Error: solo texto en rojo sin recuadro ni fondo */}
        {errorResponse && (
          <div
            role="alert"
            aria-live="assertive"
            className="mb-5 py-2 text-[#D32F2F] flex items-start justify-between gap-3 animate-in fade-in duration-150"
          >
            <div className="flex items-start gap-2.5 flex-1 min-w-0">
              <div className="shrink-0 mt-0.5">
                <Icon name="error" size={20} className="text-[#D32F2F]" />
              </div>

              <div className="flex-1 min-w-0 space-y-1.5">
                <p className="text-sm sm:text-base font-semibold text-[#D32F2F] leading-snug">
                  {errorResponse.mensaje || 'El archivo CSV contiene errores de validación.'}
                  {errorResponse.errores && errorResponse.errores.length > 0 && (
                    <span className="font-normal text-red-600 ml-1">
                      Por favor revise los formatos y reglas especificados abajo.
                    </span>
                  )}
                </p>

                {/* Detalle opcional desplegable */}
                {isErrorDetailsOpen && errorResponse.errores && errorResponse.errores.length > 0 && (
                  <div className="pt-1.5 mt-1 animate-in fade-in duration-150">
                    <p className="text-xs font-bold uppercase tracking-wider mb-1 text-[#D32F2F]">
                      Observaciones:
                    </p>
                    <ul className="list-disc list-inside space-y-1 text-xs text-red-700 max-h-40 overflow-y-auto pr-2">
                      {errorResponse.errores.map((e, idx) => {
                        let textoError = '';
                        if (e.fila !== null) {
                          textoError = `Fila ${e.fila}: ${e.mensaje}`;
                        } else if (e.campo !== null) {
                          textoError = `Campo "${e.campo}": ${e.mensaje}`;
                        } else {
                          textoError = e.mensaje;
                        }

                        return (
                          <li key={`error-item-${idx}`} className="leading-snug">
                            <span>{textoError}</span>
                          </li>
                        );
                      })}
                    </ul>
                  </div>
                )}
              </div>
            </div>

            {/* Acciones fijas en la esquina superior derecha: Ver detalle y cerrar */}
            <div className="flex items-center gap-3 shrink-0 pt-0.5">
              {errorResponse.errores && errorResponse.errores.length > 0 && (
                <button
                  type="button"
                  onClick={() => setIsErrorDetailsOpen((prev) => !prev)}
                  className="text-xs font-bold text-[#D32F2F] hover:underline flex items-center justify-end gap-1 cursor-pointer select-none min-w-[75px]"
                  aria-expanded={isErrorDetailsOpen}
                >
                  <span>{isErrorDetailsOpen ? 'Ocultar' : 'Ver detalle'}</span>
                  <Icon
                    name="chevron_down"
                    size={14}
                    className={`transition-transform duration-200 ${isErrorDetailsOpen ? 'rotate-180' : ''}`}
                  />
                </button>
              )}

              <button
                type="button"
                onClick={() => {
                  setErrorResponse(null);
                  setIsErrorDetailsOpen(false);
                }}
                className="p-0.5 text-red-400 hover:text-[#D32F2F] rounded-md transition-colors cursor-pointer"
                title="Cerrar aviso"
                aria-label="Cerrar aviso de error"
              >
                <Icon name="close" size={18} />
              </button>
            </div>
          </div>
        )}

        {/* Input file nativo oculto */}
        <input
          ref={fileInputRef}
          type="file"
          accept=".csv"
          className="hidden"
          disabled={isSubmitting}
          onChange={handleFileChange}
        />

        {/* Dropzone principal punteado */}
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => !isSubmitting && !selectedFile && fileInputRef.current?.click()}
          onKeyDown={(e) => {
            if ((e.key === 'Enter' || e.key === ' ') && !isSubmitting && !selectedFile) {
              fileInputRef.current?.click();
            }
          }}
          tabIndex={!selectedFile ? 0 : -1}
          role={!selectedFile ? 'button' : 'region'}
          aria-label={!selectedFile ? 'Seleccionar o arrastrar archivo CSV de candidatos' : 'Archivo seleccionado'}
          className={`relative border-2 border-dashed rounded-2xl transition-all select-none focus:outline-none focus:ring-2 focus:ring-[#3F2581] ${
            !selectedFile
              ? isDragging
                ? 'border-[#3F2581] bg-[#EADDFF]/40 scale-[0.99] p-8 sm:p-12 text-center cursor-pointer'
                : 'border-[#D0BCFF] bg-[#F9F7FD]/60 hover:bg-[#F4F0FF] hover:border-[#3F2581] p-8 sm:p-12 text-center cursor-pointer'
              : 'border-[#D0BCFF] bg-[#F9F7FD]/70 p-6 sm:p-8 cursor-default'
          }`}
        >
          {!selectedFile ? (
            <div className="flex flex-col items-center justify-center space-y-4">
              <div className="w-16 h-16 rounded-full bg-[#EADDFF] flex items-center justify-center text-[#3F2581] shadow-inner">
                <Icon name="upload" size={32} />
              </div>

              <div className="space-y-1.5">
                <p className="text-base sm:text-lg font-semibold text-[#3F2581]">
                  Arrastre y suelte su archivo CSV aquí, o{' '}
                  <span className="text-[#896ECB] underline underline-offset-2">haga clic para examinar</span>
                </p>
                <p className="text-xs sm:text-sm text-gray-500">
                  Formato permitido: exclusivamente archivos <strong>.csv</strong> delimitados por punto y coma (;) &bull; Máx. 5 MB
                </p>
              </div>
            </div>
          ) : (
            /* Estado con archivo seleccionado dentro del dropzone */
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-4 w-full sm:w-auto">
                <div className="w-14 h-14 rounded-xl bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shrink-0 shadow-inner">
                  <Icon name="file_text" size={30} />
                </div>
                <div className="overflow-hidden">
                  <p className="text-base font-bold text-gray-900 truncate" title={selectedFile.name}>
                    {selectedFile.name}
                  </p>
                  <p className="text-xs sm:text-sm text-gray-500 mt-0.5">
                    Tamaño: {formatFileSize(selectedFile.size)} &bull; Formato CSV
                  </p>
                  <span className="inline-flex items-center gap-1 mt-1 text-xs font-semibold text-[#1B8354]">
                    <Icon name="check" size={14} /> Archivo preparado para importación
                  </span>
                </div>
              </div>

              {/* Botón × para quitarlo dentro del dropzone y botón para cambiar */}
              <div className="flex items-center gap-2.5 self-end sm:self-center">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={isSubmitting}
                  onClick={(e) => {
                    e.stopPropagation();
                    fileInputRef.current?.click();
                  }}
                  className="text-xs sm:text-sm font-semibold"
                >
                  Cambiar archivo
                </Button>
                <button
                  type="button"
                  onClick={handleRemoveFile}
                  disabled={isSubmitting}
                  title="Quitar archivo seleccionado"
                  aria-label="Quitar archivo seleccionado"
                  className="w-8 h-8 rounded-full flex items-center justify-center text-gray-500 hover:text-red-600 hover:bg-red-50 border border-gray-300 transition-colors cursor-pointer disabled:opacity-50 disabled:pointer-events-none"
                >
                  <Icon name="close" size={16} />
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Indicador de progreso mientras dura el proceso de importación */}
        {isSubmitting && (
          <div className="mt-6 p-4 sm:p-5 bg-purple-50 border border-[#D0BCFF] rounded-2xl flex flex-col gap-3 animate-in fade-in duration-200">
            <div className="flex items-center gap-3">
              <div className="text-[#3F2581] animate-spin">
                <Icon name="timer" size={24} />
              </div>
              <div>
                <p className="text-sm font-bold text-[#3F2581]">
                  Importando y procesando candidatos...
                </p>
                <p className="text-xs text-gray-600">
                  Esta operación puede demorar algunos segundos mientras se validan y persisten los registros. Puede cancelar la operación si lo desea.
                </p>
              </div>
            </div>
            {/* Barra de progreso visual */}
            <div className="w-full bg-[#EADDFF] h-2 rounded-full overflow-hidden">
              <div className="bg-[#3F2581] h-full rounded-full animate-pulse w-full transition-all duration-1000" />
            </div>
          </div>
        )}

        {/* Bloque de estructura CSV */}
        <div className="mt-8 p-5 bg-gray-50 rounded-2xl border border-gray-200">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
            <h4 className="text-xs font-bold text-gray-700 uppercase tracking-wider flex items-center gap-1.5">
              <Icon name="table" size={16} className="text-[#3F2581]" />
              Estructura de columnas requerida
            </h4>
            <button
              type="button"
              onClick={handleCopiarEncabezados}
              className="text-xs text-gray-600 hover:text-[#3F2581] font-semibold flex items-center gap-1 cursor-pointer transition-colors self-start sm:self-auto"
              title="Copiar encabezados al portapapeles"
            >
              <Icon name={isCopied ? 'check' : 'copy'} size={14} className={isCopied ? 'text-green-600' : 'text-gray-500'} />
              <span>{isCopied ? 'Encabezados copiados' : 'Copiar encabezados'}</span>
            </button>
          </div>

          {/* Bloque de código simple y limpio para lectura directa */}
          <div className="rounded-xl border border-gray-200 bg-white p-3.5 sm:p-4 overflow-x-auto shadow-2xs">
            <pre className="text-xs text-gray-800 font-mono whitespace-pre select-all">
              {ENCABEZADOS_REQUERIDOS}
            </pre>
          </div>

          {/* Acordeón colapsado con reglas del CU006 */}
          <div className="mt-3.5 border-t border-gray-200 pt-3">
            <button
              type="button"
              onClick={() => setIsFormatAccordionOpen((prev) => !prev)}
              className="w-full flex items-center justify-between text-xs font-semibold text-gray-700 hover:text-[#3F2581] transition-colors py-1 cursor-pointer"
              aria-expanded={isFormatAccordionOpen}
            >
              <span className="flex items-center gap-1.5">
                <Icon name="help_outline" size={15} className="text-gray-500" />
                Ver formatos válidos por columna
              </span>
              <Icon
                name="chevron_down"
                size={16}
                className={`transition-transform duration-200 ${isFormatAccordionOpen ? 'rotate-180 text-[#3F2581]' : 'text-gray-400'}`}
              />
            </button>

            {isFormatAccordionOpen && (
              <div className="mt-2.5 p-3.5 bg-white rounded-xl border border-gray-200 text-xs text-gray-600 space-y-2 animate-in fade-in duration-150">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2">
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Nro candidato:</span> Clave única identificatoria.
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Apellido y Nombre:</span> Texto obligatorio en cada columna.
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Tipo documento:</span> Permitidos: <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">DNI</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">LE</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">LC</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">PP</code>.
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Nro documento:</span> Dígitos identificatorios.
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Fecha de nacimiento:</span> Formato ISO <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">AAAA-MM-DD</code> (ej. 1990-05-14).
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Género:</span> <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">H</code> (Hombre) o <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">M</code> (Mujer).
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Nacionalidad:</span> Países o gentilicios de Latinoamérica (ej: <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Argentina</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Argentino/a</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Brasil</code>, etc.).
                  </div>
                  <div>
                    <span className="font-bold text-gray-800 font-mono">Email:</span> Formato de correo electrónico válido.
                  </div>
                  <div className="sm:col-span-2">
                    <span className="font-bold text-gray-800 font-mono">Escolaridad:</span> Niveles: <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Primario</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Secundario</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Terciario</code>, <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Universitario</code> (y variantes <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Completo</code> / <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">Incompleto</code>).
                  </div>
                  <div className="sm:col-span-2">
                    <span className="font-bold text-gray-800 font-mono">Eliminado:</span> Booleano <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">True</code> (baja lógica) o <code className="bg-[#EADDFF]/60 text-[#3F2581] px-1.5 py-0.5 rounded font-mono font-medium">False</code>.
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Barra de acciones: Cancelar e Importar agrupados a la derecha */}
        <ActionToolbar align="right" className="mt-8">
          <Button
            type="button"
            variant="secondary"
            onClick={handleCancelar}
            className="font-semibold text-sm sm:text-base px-6 py-2.5"
          >
            Cancelar
          </Button>

          <Button
            type="button"
            variant={selectedFile ? 'primary' : 'secondary'}
            onClick={handleImportar}
            disabled={!selectedFile || isSubmitting}
            isLoading={isSubmitting}
            leftIcon="upload"
            className={`font-semibold text-sm sm:text-base px-8 py-2.5 ${
              !selectedFile
                ? 'opacity-60 cursor-not-allowed bg-gray-200 text-gray-500 border border-gray-300'
                : 'shadow-md'
            }`}
          >
            Importar
          </Button>
        </ActionToolbar>
      </div>

      {/* Modal de Resultado Exitoso */}
      <ModalResultadoImportacion
        isOpen={isSuccessModalOpen}
        data={resultadoExitoso}
        onAceptar={handleModalAceptar}
      />
    </div>
  );
};

export default ImportarCandidatosForm;
