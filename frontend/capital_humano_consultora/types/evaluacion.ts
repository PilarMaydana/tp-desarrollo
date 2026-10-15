export type EstadoEvaluacion = 'in-progress' | 'finished' | 'expired';

export interface Evaluacion {
  id: string;
  nroCuestionario: number;
  titulo: string;
  puestoNombre: string;
  empresaNombre?: string;
  estado: EstadoEvaluacion;
  fechaInicio: string;
  fechaFin?: string;
  ultimaActualizacion: string;
  tiempoRestante?: string;
  totalPreguntas?: number;
  preguntasRespondidas?: number;
}
