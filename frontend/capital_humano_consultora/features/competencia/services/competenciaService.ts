import { apiFetch, ApiError } from '@/services/apiClient';
import { CompetenciaCreateRequest, CompetenciaResponse } from '@/types/competencia';

export interface CompetenciaCatalogoItem extends CompetenciaResponse {
  factoresCount?: number;
  estado?: 'ACTIVA' | 'INACTIVA';
}

const SESSION_STORAGE_KEY = 'ch_competencias_locales';

const SEED_COMPETENCIAS_FALLBACK: CompetenciaCatalogoItem[] = [
  {
    id: 1,
    codigo: 'COM-01',
    nombre: 'COMUNICACIÓN',
    descripcion: 'CAPACIDAD PARA TRANSMITIR IDEAS E INFORMACIÓN DE MANERA CLARA Y EFECTIVA.',
    factoresCount: 2,
    estado: 'ACTIVA',
  },
  {
    id: 2,
    codigo: 'LID-01',
    nombre: 'LIDERAZGO',
    descripcion: 'CAPACIDAD PARA GUIAR, MOTIVAR Y DIRIGIR EQUIPOS DE TRABAJO HACIA EL LOGRO DE OBJETIVOS.',
    factoresCount: 0,
    estado: 'ACTIVA',
  },
  {
    id: 3,
    codigo: 'TRAB-01',
    nombre: 'TRABAJO EN EQUIPO',
    descripcion: 'CAPACIDAD PARA COLABORAR DE MANERA EFICAZ CON OTRAS PERSONAS PARA EL LOGRO DE METAS COMUNES.',
    factoresCount: 0,
    estado: 'ACTIVA',
  },
  {
    id: 4,
    codigo: 'RESOL-01',
    nombre: 'RESOLUCIÓN DE PROBLEMAS',
    descripcion: 'CAPACIDAD PARA IDENTIFICAR PROBLEMAS COMPLEJOS Y EVALUAR OPCIONES DE SOLUCIÓN.',
    factoresCount: 0,
    estado: 'ACTIVA',
  },
  {
    id: 5,
    codigo: 'TEC-01',
    nombre: 'COMPETENCIAS TÉCNICAS',
    descripcion: 'CAPACIDAD PARA APLICAR CONOCIMIENTOS TÉCNICOS EN EL DESARROLLO DE SOFTWARE.',
    factoresCount: 1,
    estado: 'ACTIVA',
  },
];

function getLocalFallbackCompetencias(): CompetenciaCatalogoItem[] {
  if (typeof window === 'undefined') {
    return SEED_COMPETENCIAS_FALLBACK;
  }
  try {
    const raw = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) return SEED_COMPETENCIAS_FALLBACK;
    const extras = JSON.parse(raw) as CompetenciaCatalogoItem[];
    if (!Array.isArray(extras)) return SEED_COMPETENCIAS_FALLBACK;
    const map = new Map<string, CompetenciaCatalogoItem>();
    SEED_COMPETENCIAS_FALLBACK.forEach((item) => map.set(item.codigo, item));
    extras.forEach((item) => map.set(item.codigo, item));
    return Array.from(map.values());
  } catch {
    return SEED_COMPETENCIAS_FALLBACK;
  }
}

function saveLocalFallbackCompetencia(nueva: CompetenciaCatalogoItem): void {
  if (typeof window === 'undefined') return;
  try {
    const raw = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    const extras: CompetenciaCatalogoItem[] = raw ? JSON.parse(raw) : [];
    extras.push(nueva);
    window.sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(extras));
  } catch {
    // Ignore storage errors
  }
}

export const competenciaService = {
  /**
   * Obtiene el listado completo de competencias registradas (GET /api/competencias)
   * y consulta la cantidad de factores asociados a cada una.
   */
  async listarCompetencias(): Promise<CompetenciaCatalogoItem[]> {
    try {
      const data = await apiFetch<CompetenciaResponse[]>('/competencias');
      if (!Array.isArray(data)) {
        return getLocalFallbackCompetencias();
      }

      // Enriquecer con conteo de factores si el endpoint está disponible
      const enriched = await Promise.all(
        data.map(async (comp) => {
          try {
            const factores = await apiFetch<unknown[]>(
              `/competencias/${encodeURIComponent(comp.codigo)}/factores`
            );
            return {
              ...comp,
              factoresCount: Array.isArray(factores) ? factores.length : 0,
              estado: 'ACTIVA' as const,
            };
          } catch {
            return {
              ...comp,
              factoresCount: 0,
              estado: 'ACTIVA' as const,
            };
          }
        })
      );

      return enriched;
    } catch {
      return getLocalFallbackCompetencias();
    }
  },

  /**
   * Da de alta una nueva competencia (POST /api/competencias - CU 013).
   */
  async crearCompetencia(payload: CompetenciaCreateRequest): Promise<CompetenciaResponse> {
    const normalizado: CompetenciaCreateRequest = {
      codigo: payload.codigo.trim().toUpperCase(),
      nombre: payload.nombre.trim().toUpperCase(),
      descripcion: payload.descripcion.trim().toUpperCase(),
    };

    try {
      const response = await apiFetch<CompetenciaResponse>('/competencias', {
        method: 'POST',
        body: JSON.stringify(normalizado),
      });
      return response;
    } catch (err: unknown) {
      if (
        err instanceof ApiError &&
        err.status === 500 &&
        (err.message.includes('conexión') || err.message.includes('fetch'))
      ) {
        // Fallback local cuando el backend no está levantado
        const existentes = getLocalFallbackCompetencias();
        const dupCodigo = existentes.find((c) => c.codigo.toUpperCase() === normalizado.codigo);
        if (dupCodigo) {
          throw new ApiError(409, `Ya existe una competencia con el código '${normalizado.codigo}'`, {
            mensaje: `Ya existe una competencia con el código '${normalizado.codigo}'`,
          });
        }
        const dupNombre = existentes.find((c) => c.nombre.toUpperCase() === normalizado.nombre);
        if (dupNombre) {
          throw new ApiError(409, `Ya existe una competencia con el nombre '${normalizado.nombre}'`, {
            mensaje: `Ya existe una competencia con el nombre '${normalizado.nombre}'`,
          });
        }

        const creada: CompetenciaCatalogoItem = {
          id: Date.now(),
          codigo: normalizado.codigo,
          nombre: normalizado.nombre,
          descripcion: normalizado.descripcion,
          factoresCount: 0,
          estado: 'ACTIVA',
        };
        saveLocalFallbackCompetencia(creada);
        return creada;
      }
      throw err;
    }
  },
};

export { ApiError };

