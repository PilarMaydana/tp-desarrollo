import { Evaluacion } from '@/types/evaluacion';
import { apiFetch } from './apiClient';

const MOCK_EVALUACIONES: Evaluacion[] = [
  {
    id: 'eval-1',
    nroCuestionario: 1,
    titulo: 'Cuestionario 1',
    puestoNombre: 'Desarrollador Full Stack Senior',
    empresaNombre: 'Tech Solutions S.A.',
    estado: 'in-progress',
    fechaInicio: '10/03/2026 14:00',
    ultimaActualizacion: '12/03/2026 18:30',
    tiempoRestante: '45 minutos restantes',
    totalPreguntas: 30,
    preguntasRespondidas: 18,
  },
  {
    id: 'eval-2',
    nroCuestionario: 2,
    titulo: 'Cuestionario 2',
    puestoNombre: 'Analista de Recursos Humanos',
    empresaNombre: 'Consultores Asociados',
    estado: 'finished',
    fechaInicio: '01/03/2026 09:15',
    fechaFin: '01/03/2026 10:20',
    ultimaActualizacion: '01/03/2026 10:20',
    totalPreguntas: 25,
    preguntasRespondidas: 25,
  },
  {
    id: 'eval-3',
    nroCuestionario: 3,
    titulo: 'Cuestionario 3',
    puestoNombre: 'Gerente de Operaciones',
    empresaNombre: 'Logística Global',
    estado: 'expired',
    fechaInicio: '15/02/2026 11:00',
    ultimaActualizacion: '18/02/2026 23:59',
    totalPreguntas: 35,
    preguntasRespondidas: 8,
  },
];

export const evaluacionService = {
  async getEvaluacionesCandidato(): Promise<Evaluacion[]> {
    try {
      return await apiFetch<Evaluacion[]>('/evaluaciones/mis-cuestionarios');
    } catch {
      return MOCK_EVALUACIONES;
    }
  },
};
