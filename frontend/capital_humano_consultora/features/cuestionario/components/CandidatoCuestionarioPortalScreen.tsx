'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { EvaluationCard } from '@/components/organisms/EvaluationCard';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ROUTES } from '@/constants/routes';
import { useAuth } from '@/context/AuthContext';
import type { EstadoEvaluacion, Evaluacion } from '@/types/evaluacion';
import { ApiError, cuestionarioService } from '../services/cuestionarioService';
import type {
  BackendApiErrorData,
  EstadoCuestionario,
  InstruccionesCuestionarioDTO,
} from '../types';

function mapearEstadoEvaluacion(estado: EstadoCuestionario): EstadoEvaluacion {
  switch (estado) {
    case 'ACTIVO':
      return 'active';
    case 'EN_PROCESO':
      return 'in-progress';
    case 'COMPLETO':
      return 'finished';
    case 'INCOMPLETO':
    case 'SIN_CONTESTAR':
    default:
      return 'expired';
  }
}

function mapearInstruccionesAEvaluacion(
  dto: InstruccionesCuestionarioDTO
): Evaluacion {
  const estado = mapearEstadoEvaluacion(dto.estado);
  return {
    id: String(dto.id),
    nroCuestionario: dto.id,
    titulo: 'Cuestionario de Evaluación de Competencias',
    puestoNombre: dto.nombrePuesto || 'Puesto asignado',
    estado,
    fechaInicio: estado === 'in-progress' ? 'En curso' : 'Sin iniciar',
    ultimaActualizacion: 'Hoy',
    tiempoRestante: `Tiempo máximo disponible: ${dto.tiempoMaximoMinutos} ${
      dto.tiempoMaximoMinutos === 1 ? 'minuto' : 'minutos'
    }`,
  };
}

/**
 * Portal del candidato (/candidato/cuestionario).
 * Consulta el cuestionario real asignado al candidato autenticado (sin consumir accesos)
 * y al presionar «Comenzar» / «Continuar» navega a /cuestionario/[id] (CU 026).
 */
export const CandidatoCuestionarioPortalScreen: React.FC = () => {
  const { user } = useAuth();
  const router = useRouter();

  const cuestionarioId = user?.cuestionarioId;

  const [evaluacion, setEvaluacion] = useState<Evaluacion | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(Boolean(cuestionarioId));
  const [estadoNoDisponible, setEstadoNoDisponible] = useState<string | null>(null);
  const [errorConexion, setErrorConexion] = useState<string | null>(null);

  const loadedIdRef = useRef<number | null>(null);

  const cargarCuestionario = useCallback(async (id: number) => {
    setIsLoading(true);
    setEvaluacion(null);
    setEstadoNoDisponible(null);
    setErrorConexion(null);

    try {
      const dto = await cuestionarioService.obtenerInstrucciones(id);
      setEvaluacion(mapearInstruccionesAEvaluacion(dto));
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const data =
          err.data && typeof err.data === 'object'
            ? (err.data as BackendApiErrorData)
            : null;
        const mensaje = data?.mensaje || err.message;

        if (
          err.status === 422 ||
          err.status === 404 ||
          data?.codigo === 'REGLA_NEGOCIO' ||
          data?.codigo === 'RECURSO_NO_ENCONTRADO'
        ) {
          setEstadoNoDisponible(mensaje);
          return;
        }

        setErrorConexion(mensaje);
        return;
      }

      setErrorConexion(
        err instanceof Error && err.message
          ? err.message
          : 'No fue posible comunicarse con el servidor.'
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!cuestionarioId) {
      loadedIdRef.current = null;
      return;
    }

    if (loadedIdRef.current === cuestionarioId) {
      return;
    }
    loadedIdRef.current = cuestionarioId;

    void cargarCuestionario(cuestionarioId);
  }, [cuestionarioId, cargarCuestionario]);

  const handleAction = (item: Evaluacion) => {
    router.push(ROUTES.CANDIDATO.CUESTIONARIO_COMPLETAR(item.nroCuestionario));
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
          {evaluacion?.estado === 'in-progress'
            ? 'Cuestionario en Proceso'
            : 'Cuestionario Asignado'}
        </h1>
        <p className="text-sm md:text-base text-[#555555] mt-1 font-medium">
          {evaluacion?.estado === 'in-progress'
            ? 'Presione «Continuar» para reanudar la evaluación.'
            : 'Presione «Comenzar» para iniciar la evaluación.'}
        </p>
      </div>

      {!cuestionarioId ? (
        <div className="p-8 text-center text-[#757575] bg-white rounded-xl border border-gray-200">
          No posee un cuestionario activo asignado en su sesión actual.
        </div>
      ) : isLoading ? (
        <div className="p-12 bg-white rounded-xl border border-gray-200 flex flex-col items-center justify-center gap-3 text-[#757575]">
          <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Cargando cuestionario...</span>
        </div>
      ) : errorConexion ? (
        <div
          role="alert"
          className="p-8 bg-white rounded-2xl border-2 border-red-200 flex flex-col items-center text-center gap-4"
        >
          <div className="w-12 h-12 rounded-full bg-red-50 text-[#D32F2F] flex items-center justify-center">
            <Icon name="error" size={26} />
          </div>
          <div className="space-y-1">
            <h2 className="text-lg font-bold text-[#3F2581]">
              Error al consultar el cuestionario
            </h2>
            <p className="text-sm text-[#4A4A4A]">{errorConexion}</p>
          </div>
          <Button
            type="button"
            variant="primary"
            onClick={() => void cargarCuestionario(cuestionarioId)}
          >
            Reintentar
          </Button>
        </div>
      ) : estadoNoDisponible ? (
        <div
          role="status"
          className="p-8 bg-white rounded-2xl border border-[#E0E0E0] flex flex-col items-center text-center gap-3"
        >
          <div className="w-12 h-12 rounded-full bg-[#F4F0FF] text-[#3F2581] flex items-center justify-center">
            <Icon name="assignment" size={26} />
          </div>
          <h2 className="text-lg font-bold text-[#3F2581]">
            Cuestionario no disponible
          </h2>
          <p className="text-sm text-[#4A4A4A] max-w-lg">{estadoNoDisponible}</p>
        </div>
      ) : evaluacion ? (
        <div className="flex flex-col gap-4">
          <EvaluationCard evaluacion={evaluacion} onAction={handleAction} />
        </div>
      ) : (
        <div className="p-8 text-center text-[#757575] bg-white rounded-xl border border-gray-200">
          No posee un cuestionario activo en este momento.
        </div>
      )}
    </div>
  );
};

export default CandidatoCuestionarioPortalScreen;
