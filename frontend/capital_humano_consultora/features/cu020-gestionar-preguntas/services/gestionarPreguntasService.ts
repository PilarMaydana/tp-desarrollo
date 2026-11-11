import { apiFetch, ApiError } from '@/services/apiClient';
import {
  FiltrosBusquedaPregunta,
  Pregunta,
  CompetenciaItem,
  FactorItem,
  BackendApiErrorData,
} from '../types';

/**
 * Datos de respaldo (Stubs) para desarrollo aislado en Sandbox (CDD).
 * Permiten probar todos los flujos de búsqueda, selección, navegación y baja
 * incluso si el backend Spring Boot se encuentra apagado o no inicializado.
 */
const MOCK_COMPETENCIAS: CompetenciaItem[] = [
  { id: 1, codigo: 'COM-01', nombre: 'COMUNICACIÓN', descripcion: 'Capacidad para transmitir ideas de forma clara y efectiva.' },
  { id: 2, codigo: 'LID-01', nombre: 'LIDERAZGO', descripcion: 'Capacidad para orientar y motivar equipos hacia metas compartidas.' },
  { id: 3, codigo: 'TRAB-01', nombre: 'TRABAJO EN EQUIPO', descripcion: 'Capacidad para cooperar constructivamente con otros integrantes.' },
  { id: 4, codigo: 'RESOL-01', nombre: 'RESOLUCIÓN DE PROBLEMAS', descripcion: 'Capacidad para analizar situaciones y diseñar soluciones eficaces.' },
  { id: 5, codigo: 'PROACT-01', nombre: 'PROACTIVIDAD E INICIATIVA', descripcion: 'Capacidad para anticipar necesidades y actuar oportunamente.' },
];

const MOCK_FACTORES: FactorItem[] = [
  { id: 1, codigo: 'FAC-COM-1', nombre: 'COMUNICACIÓN ORAL', competenciaId: 1 },
  { id: 2, codigo: 'FAC-COM-2', nombre: 'COMUNICACIÓN ESCRITA', competenciaId: 1 },
  { id: 3, codigo: 'FAC-LID-1', nombre: 'TOMA DE DECISIONES', competenciaId: 2 },
  { id: 4, codigo: 'FAC-LID-2', nombre: 'DELEGACIÓN Y SEGUIMIENTO', competenciaId: 2 },
  { id: 5, codigo: 'FAC-TRAB-1', nombre: 'COOPERACIÓN INTERPERSONAL', competenciaId: 3 },
  { id: 6, codigo: 'FAC-TRAB-2', nombre: 'RESOLUCIÓN DE CONFLICTOS', competenciaId: 3 },
  { id: 7, codigo: 'FAC-RES-1', nombre: 'ANÁLISIS CRÍTICO', competenciaId: 4 },
  { id: 8, codigo: 'FAC-RES-2', nombre: 'PENSAMIENTO LÓGICO', competenciaId: 4 },
  { id: 9, codigo: 'FAC-PRO-1', nombre: 'ORIENTACIÓN A RESULTADOS', competenciaId: 5 },
];

let mockPreguntasMemoria: Pregunta[] = [
  {
    id: 101,
    competencia: 'COMUNICACIÓN',
    factor: 'COMUNICACIÓN ORAL',
    factorCodigo: 'FAC-COM-1',
    nombre: 'Explicación de conceptos técnicos a clientes',
    enunciado: '¿Cómo adapta su lenguaje técnico al interactuar con interlocutores o clientes sin conocimientos especializados?',
    descripcion: 'Evalúa la capacidad de simplificación y claridad en la comunicación verbal.',
    opciones: [
      { id: 1, nombre: 'Utilizo analogías sencillas y verifico la comprensión frecuentemente.', ponderacion: 10, nroOrden: 1 },
      { id: 2, nombre: 'Explico los términos técnicos a medida que surgen en la conversación.', ponderacion: 7, nroOrden: 2 },
      { id: 3, nombre: 'Transmito el informe tal cual está escrito y consulto si hay dudas.', ponderacion: 4, nroOrden: 3 },
    ],
  },
  {
    id: 102,
    competencia: 'COMUNICACIÓN',
    factor: 'COMUNICACIÓN ESCRITA',
    factorCodigo: 'FAC-COM-2',
    nombre: 'Documentación estructurada de contingencias',
    enunciado: '¿Qué método o estructura sigue al redactar un informe formal tras un incidente imprevisto?',
    descripcion: 'Mide la precisión, concisión y formalidad documental.',
    opciones: [
      { id: 4, nombre: 'Estructuro el reporte detallando antecedentes, causa raíz, medidas y próximos pasos.', ponderacion: 10, nroOrden: 1 },
      { id: 5, nombre: 'Redacto un resumen cronológico directo para los involucrados.', ponderacion: 7, nroOrden: 2 },
      { id: 6, nombre: 'Envío notas rápidas con lo acontecido por correo.', ponderacion: 3, nroOrden: 3 },
    ],
  },
  {
    id: 103,
    competencia: 'LIDERAZGO',
    factor: 'TOMA DE DECISIONES',
    factorCodigo: 'FAC-LID-1',
    nombre: 'Decisiones críticas bajo incertidumbre',
    enunciado: '¿Cómo procede cuando debe tomar una resolución de impacto inmediato y no cuenta con la totalidad de los datos?',
    descripcion: 'Evalúa la templanza analítica y la asunción de riesgos calculados.',
    opciones: [
      { id: 7, nombre: 'Evalúo escenarios probables, consulto fuentes clave y decido minimizando el impacto negativo.', ponderacion: 10, nroOrden: 1 },
      { id: 8, nombre: 'Adopto una solución estándar ya probada en casos similares anteriores.', ponderacion: 7, nroOrden: 2 },
      { id: 9, nombre: 'Demoro la decisión hasta que se recopilen datos concluyentes.', ponderacion: 2, nroOrden: 3 },
    ],
  },
  {
    id: 104,
    competencia: 'LIDERAZGO',
    factor: 'DELEGACIÓN Y SEGUIMIENTO',
    factorCodigo: 'FAC-LID-2',
    nombre: 'Distribución y supervisión de tareas críticas',
    enunciado: '¿Qué criterios utiliza para delegar actividades prioritarias sin recurrir al micromanagement?',
    descripcion: 'Mide la confianza en el equipo y la claridad en la fijación de indicadores.',
    opciones: [
      { id: 10, nombre: 'Asigno según fortalezas, acuerdo metas claras y establezco hitos de revisión periódicos.', ponderacion: 10, nroOrden: 1 },
      { id: 11, nombre: 'Delego tareas operativas y mantengo las estratégicas bajo control directo diario.', ponderacion: 6, nroOrden: 2 },
      { id: 12, nombre: 'Distribuyo las tareas por orden de llegada o disponibilidad.', ponderacion: 3, nroOrden: 3 },
    ],
  },
  {
    id: 105,
    competencia: 'TRABAJO EN EQUIPO',
    factor: 'COOPERACIÓN INTERPERSONAL',
    factorCodigo: 'FAC-TRAB-1',
    nombre: 'Articulación con diferentes perfiles profesionales',
    enunciado: '¿Cómo coordina actividades cuando los miembros del equipo tienen perfiles y ritmos de trabajo dispares?',
    descripcion: 'Evalúa la flexibilidad interpersonal y la sinergia grupal.',
    opciones: [
      { id: 13, nombre: 'Fomento espacios de alineación para complementar las habilidades de cada perfil.', ponderacion: 10, nroOrden: 1 },
      { id: 14, nombre: 'Me adapto al ritmo de la mayoría para evitar fricciones.', ponderacion: 7, nroOrden: 2 },
      { id: 15, nombre: 'Prefiero enfocarme en mis entregables individuales.', ponderacion: 3, nroOrden: 3 },
    ],
  },
  {
    id: 106,
    competencia: 'TRABAJO EN EQUIPO',
    factor: 'RESOLUCIÓN DE CONFLICTOS',
    factorCodigo: 'FAC-TRAB-2',
    nombre: 'Gestión de discrepancias metodológicas internas',
    enunciado: 'Relate cómo actúa frente a posturas enfrentadas sobre la metodología para encarar un proyecto.',
    descripcion: 'Mide la búsqueda de consensos constructivos.',
    opciones: [
      { id: 16, nombre: 'Propongo analizar pros y contras objetivos basados en datos para consensuar.', ponderacion: 10, nroOrden: 1 },
      { id: 17, nombre: 'Elevo el desacuerdo al líder directo para que determine el curso.', ponderacion: 6, nroOrden: 2 },
      { id: 18, nombre: 'Acepto la postura contraria para no demorar la entrega.', ponderacion: 3, nroOrden: 3 },
    ],
  },
  {
    id: 107,
    competencia: 'RESOLUCIÓN DE PROBLEMAS',
    factor: 'ANÁLISIS CRÍTICO',
    factorCodigo: 'FAC-RES-1',
    nombre: 'Detección sistemática de causas raíz',
    enunciado: '¿Qué técnicas o métodos implementa al enfrentarse a una recurrencia inesperada de fallas?',
    descripcion: 'Evalúa el razonamiento analítico estructurado.',
    opciones: [
      { id: 19, nombre: 'Aplico los 5 Porqués y diagramas causa-efecto con el equipo de soporte.', ponderacion: 10, nroOrden: 1 },
      { id: 20, nombre: 'Reviso los logs recientes y aplico parches preventivos.', ponderacion: 7, nroOrden: 2 },
      { id: 21, nombre: 'Reinicio los servicios y monitoreo si el error vuelve a manifestarse.', ponderacion: 2, nroOrden: 3 },
    ],
  },
  {
    id: 108,
    competencia: 'PROACTIVIDAD E INICIATIVA',
    factor: 'ORIENTACIÓN A RESULTADOS',
    factorCodigo: 'FAC-PRO-1',
    nombre: 'Implementación autónoma de mejoras de procesos',
    enunciado: 'Describa una iniciativa que haya liderado sin que mediara una instrucción expresa de sus superiores.',
    descripcion: 'Evalúa la automotivación e innovación aplicada.',
    opciones: [
      { id: 22, nombre: 'Identifiqué un cuello de botella, diseñé una automatización y documenté el ahorro logrado.', ponderacion: 10, nroOrden: 1 },
      { id: 23, nombre: 'Propuse una idea en la reunión semanal y aguardé autorización para avanzar.', ponderacion: 7, nroOrden: 2 },
      { id: 24, nombre: 'Me mantengo dentro de mis funciones asignadas en el manual del puesto.', ponderacion: 3, nroOrden: 3 },
    ],
  },
];

export const gestionarPreguntasService = {
  /**
   * Busca preguntas según los criterios ingresados (GET /api/preguntas).
   * Semántica: "contiene" insensible a mayúsculas.
   * Requiere al menos un criterio no vacío.
   */
  async buscarPreguntas(filtros: FiltrosBusquedaPregunta): Promise<Pregunta[]> {
    const comp = filtros.competencia?.trim() || '';
    const fac = filtros.factor?.trim() || '';
    const nom = filtros.nombre?.trim() || '';

    try {
      const data = await apiFetch<Pregunta[]>('/preguntas', {
        params: {
          competencia: comp || undefined,
          factor: fac || undefined,
          nombre: nom || undefined,
        },
      });

      if (Array.isArray(data)) {
        return data;
      }
      return [];
    } catch {
      // Fallback en memoria si la API backend está apagada durante desarrollo
      return mockPreguntasMemoria.filter((p) => {
        const matchComp = !comp || p.competencia.toLowerCase().includes(comp.toLowerCase());
        const matchFac =
          !fac ||
          p.factor.toLowerCase().includes(fac.toLowerCase()) ||
          (p.factorCodigo && p.factorCodigo.toLowerCase().includes(fac.toLowerCase()));
        const matchNom =
          !nom ||
          p.nombre.toLowerCase().includes(nom.toLowerCase()) ||
          p.enunciado.toLowerCase().includes(nom.toLowerCase());

        return matchComp && matchFac && matchNom;
      });
    }
  },

  /**
   * Obtiene los detalles de una pregunta activa por su ID (GET /api/preguntas/{id}).
   */
  async obtenerPregunta(id: number): Promise<Pregunta> {
    try {
      return await apiFetch<Pregunta>(`/preguntas/${id}`);
    } catch {
      const encontrada = mockPreguntasMemoria.find((p) => p.id === id);
      if (encontrada) return encontrada;
      throw new ApiError(404, `No se encontró la pregunta con ID ${id}`);
    }
  },

  /**
   * Da de baja lógica una pregunta (DELETE /api/preguntas/{id}) - CU 023.
   */
  async eliminarPregunta(id: number): Promise<void> {
    try {
      await apiFetch<void>(`/preguntas/${id}`, {
        method: 'DELETE',
      });
    } catch {
      // En caso de usar fallback en memoria, actualizamos la lista local
      mockPreguntasMemoria = mockPreguntasMemoria.filter((p) => p.id !== id);
    }
  },

  /**
   * Lista las competencias disponibles para los filtros (GET /api/competencias).
   */
  async obtenerCompetencias(): Promise<CompetenciaItem[]> {
    try {
      const data = await apiFetch<CompetenciaItem[]>('/competencias');
      if (Array.isArray(data) && data.length > 0) {
        return data;
      }
      return MOCK_COMPETENCIAS;
    } catch {
      return MOCK_COMPETENCIAS;
    }
  },

  /**
   * Lista los factores disponibles para los filtros (GET /api/factores).
   */
  async obtenerFactores(competenciaId?: number): Promise<FactorItem[]> {
    try {
      const data = await apiFetch<FactorItem[]>('/factores', {
        params: competenciaId ? { competenciaId } : undefined,
      });
      if (Array.isArray(data) && data.length > 0) {
        return data;
      }
      return competenciaId
        ? MOCK_FACTORES.filter((f) => f.competenciaId === competenciaId)
        : MOCK_FACTORES;
    } catch {
      return competenciaId
        ? MOCK_FACTORES.filter((f) => f.competenciaId === competenciaId)
        : MOCK_FACTORES;
    }
  },
};

export { ApiError };
export type { BackendApiErrorData };

