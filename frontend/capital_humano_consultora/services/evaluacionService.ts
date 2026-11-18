import { Evaluacion } from '@/types/evaluacion';
import { apiFetch } from './apiClient';

export const evaluacionService = {
  async getCuestionarioActivo(cuestionarioId?: number): Promise<Evaluacion> {
    try {
      const endpoint = cuestionarioId
        ? `/evaluaciones/cuestionario/${cuestionarioId}`
        : '/evaluaciones/mis-cuestionarios';
      return await apiFetch<Evaluacion>(endpoint);
    } catch {
      // Fallback para desarrollo offline / testing

      // Si el cuestionario es el 2 (TEST4567 - María Gómez), está en proceso (reingreso para continuar)
      if (cuestionarioId === 2) {
        return {
          id: 'cuestionario-2',
          nroCuestionario: 2,
          titulo: 'Cuestionario de Evaluación de Competencias',
          puestoNombre: 'Analista de Selección y RRHH',
          empresaNombre: 'Consultora Capit@l Humano',
          estado: 'in-progress',
          fechaInicio: '18/11/2026 14:00',
          ultimaActualizacion: '18/11/2026 14:45',
          tiempoRestante: '35 minutos restantes',
          totalPreguntas: 20,
          preguntasRespondidas: 8,
        };
      }

      // Por defecto (ej. cuestionario 1 / CLAVE123 - Juan Pérez), está activo listo para comenzar
      return {
        id: 'cuestionario-1',
        nroCuestionario: 1,
        titulo: 'Cuestionario de Evaluación de Competencias',
        puestoNombre: 'Desarrollador Full Stack Senior',
        empresaNombre: 'Consultora Capit@l Humano',
        estado: 'active',
        fechaInicio: 'Sin iniciar',
        ultimaActualizacion: 'Hoy',
        tiempoRestante: 'Tiempo máximo disponible: 60 minutos',
        totalPreguntas: 20,
        preguntasRespondidas: 0,
      };
    }
  },

  // Método legacy por compatibilidad
  async getEvaluacionesCandidato(): Promise<Evaluacion[]> {
    const active = await this.getCuestionarioActivo();
    return [active];
  },
};
