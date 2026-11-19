import { apiFetch, ApiError } from '@/services/apiClient';
import type {
  ArchivoExcelDescargado,
  BackendApiErrorData,
  CandidatoResumenDTO,
  CrearEvaluacionRequestDTO,
  DetallePuestoEvaluacionDTO,
  EvaluacionCreadaDTO,
  FiltrosBusquedaCandidatos,
  PrevisualizacionEvaluacionDTO,
  PrevisualizarEvaluacionRequestDTO,
  PuestoResumenDTO,
  ValidacionCandidatosResponseDTO,
} from '../types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

/**
 * Extrae el nombre de archivo desde el header Content-Disposition.
 * Soporta filename*=UTF-8''..., filename="..." y filename=...
 */
export function extraerNombreArchivoContentDisposition(
  contentDisposition: string | null,
  fallbackFilename: string
): string {
  if (!contentDisposition) {
    return fallbackFilename;
  }

  const utf8Match = contentDisposition.match(/filename\*\s*=\s*UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    try {
      const decoded = decodeURIComponent(utf8Match[1].trim().replace(/^["']|["']$/g, ''));
      if (decoded) return decoded;
    } catch {
      // Continuar con el siguiente formato si falla decodeURIComponent
    }
  }

  const quotedMatch = contentDisposition.match(/filename\s*=\s*"([^"]+)"/i);
  if (quotedMatch?.[1]) {
    const cleaned = quotedMatch[1].trim();
    if (cleaned) return cleaned;
  }

  const unquotedMatch = contentDisposition.match(/filename\s*=\s*([^;]+)/i);
  if (unquotedMatch?.[1]) {
    const cleaned = unquotedMatch[1].trim().replace(/^["']|["']$/g, '');
    if (cleaned) return cleaned;
  }

  return fallbackFilename;
}

/**
 * Dispara la descarga de un Blob en el navegador con el nombre de archivo indicado.
 */
export function dispararDescargaArchivo(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.setTimeout(() => {
    window.URL.revokeObjectURL(url);
  }, 1000);
}

export const evaluacionService = {
  /**
   * Busca candidatos activos con filtros opcionales (CU 025 - Pasos 2 y 3).
   * GET /api/evaluaciones/candidatos?apellido=&nombre=&nroCandidato=
   */
  async buscarCandidatos(
    filtros?: FiltrosBusquedaCandidatos
  ): Promise<CandidatoResumenDTO[]> {
    const apellido = filtros?.apellido?.trim().toUpperCase() || undefined;
    const nombre = filtros?.nombre?.trim().toUpperCase() || undefined;
    const nroCandidato = filtros?.nroCandidato?.trim().toUpperCase() || undefined;

    return await apiFetch<CandidatoResumenDTO[]>('/evaluaciones/candidatos', {
      method: 'GET',
      params: {
        apellido,
        nombre,
        nroCandidato,
      },
    });
  },

  /**
   * Valida que la selección no esté vacía y que ningún candidato posea
   * cuestionarios en estado ACTIVO o EN_PROCESO (CU 025 - Pasos 4.A, 6 y 6.A).
   * POST /api/evaluaciones/validar-candidatos
   */
  async validarCandidatos(
    candidatoIds: number[]
  ): Promise<ValidacionCandidatosResponseDTO> {
    return await apiFetch<ValidacionCandidatosResponseDTO>(
      '/evaluaciones/validar-candidatos',
      {
        method: 'POST',
        body: JSON.stringify({ candidatoIds }),
      }
    );
  },

  /**
   * Lista los puestos activos disponibles para evaluar candidatos (CU 025 - Paso 8).
   * GET /api/evaluaciones/puestos
   */
  async listarPuestos(): Promise<PuestoResumenDTO[]> {
    return await apiFetch<PuestoResumenDTO[]>('/evaluaciones/puestos', {
      method: 'GET',
    });
  },

  /**
   * Obtiene competencias y ponderaciones requeridas de un puesto validando suficiencia
   * de factores y preguntas activas (CU 025 - Pasos 9 y 9.A).
   * GET /api/evaluaciones/puestos/{puestoId}/competencias
   */
  async obtenerCompetenciasPuesto(
    puestoId: number
  ): Promise<DetallePuestoEvaluacionDTO> {
    return await apiFetch<DetallePuestoEvaluacionDTO>(
      `/evaluaciones/puestos/${puestoId}/competencias`,
      {
        method: 'GET',
      }
    );
  },

  /**
   * Genera la previsualización de la evaluación y claves aleatorias sin persistir
   * (CU 025 - Paso 11 y soporte para Flujo 12.A).
   * POST /api/evaluaciones/previsualizar
   */
  async previsualizarEvaluacion(
    payload: PrevisualizarEvaluacionRequestDTO
  ): Promise<PrevisualizacionEvaluacionDTO> {
    return await apiFetch<PrevisualizacionEvaluacionDTO>(
      '/evaluaciones/previsualizar',
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    );
  },

  /**
   * Confirma y persiste atómicamente la evaluación con sus cuestionarios en estado ACTIVO
   * reutilizando las claves mostradas en la previsualización (CU 025 - Pasos 12 y 13).
   * POST /api/evaluaciones
   */
  async crearEvaluacion(
    payload: CrearEvaluacionRequestDTO
  ): Promise<EvaluacionCreadaDTO> {
    return await apiFetch<EvaluacionCreadaDTO>('/evaluaciones', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Descarga el archivo binario Excel (.xls) de una evaluación creada (CU 025 - Pasos 14-17).
   * GET /api/evaluaciones/{evaluacionId}/exportar-excel
   * No modifica apiClient para preservar compatibilidad con el resto de las pantallas.
   */
  async exportarEvaluacionExcel(
    evaluacionId: number
  ): Promise<ArchivoExcelDescargado> {
    const url = `${API_BASE_URL}/evaluaciones/${evaluacionId}/exportar-excel`;
    const headers: Record<string, string> = {};

    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('auth_token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    try {
      const response = await fetch(url, {
        method: 'GET',
        headers,
      });

      if (!response.ok) {
        const errorData = (await response.json().catch(() => null)) as
          | BackendApiErrorData
          | null;
        throw new ApiError(
          response.status,
          errorData?.mensaje ||
            `Error en la solicitud: ${response.statusText || response.status}`,
          errorData
        );
      }

      const blob = await response.blob();
      const contentDisposition = response.headers.get('Content-Disposition');
      const filename = extraerNombreArchivoContentDisposition(
        contentDisposition,
        `evaluacion-${evaluacionId}.xls`
      );

      return { blob, filename };
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(
        500,
        (error as Error).message || 'Error de conexión con el servidor'
      );
    }
  },
};

export { ApiError };
