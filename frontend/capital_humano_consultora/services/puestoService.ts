import { Competencia } from '@/types/competencia';
import { CrearPuestoDTO, Empresa, Puesto, PuestosFilterParams } from '@/types/puesto';
import { apiFetch } from './apiClient';

const MOCK_EMPRESAS: Empresa[] = [
  { id: 'emp-1', nombre: 'Tech Solutions S.A.' },
  { id: 'emp-2', nombre: 'Banco Metropolitano' },
  { id: 'emp-3', nombre: 'Consultores Asociados' },
  { id: 'emp-4', nombre: 'Logística Global' },
];

const MOCK_COMPETENCIAS: Competencia[] = [
  { id: 'comp-1', nombre: 'Liderazgo y Gestión de Equipos', descripcion: 'Capacidad para orientar y coordinar' },
  { id: 'comp-2', nombre: 'Resolución de Conflictos', descripcion: 'Habilidad para conciliar y mediar' },
  { id: 'comp-3', nombre: 'Pensamiento Analítico', descripcion: 'Capacidad para desglosar problemas complejos' },
  { id: 'comp-4', nombre: 'Comunicación Asertiva', descripcion: 'Habilidad para transmitir ideas de forma clara' },
  { id: 'comp-5', nombre: 'Adaptabilidad al Cambio', descripcion: 'Flexibilidad ante nuevos escenarios' },
  { id: 'comp-6', nombre: 'Trabajo en Equipo', descripcion: 'Colaboración efectiva con pares y superiores' },
  { id: 'comp-7', nombre: 'Orientación a Resultados', descripcion: 'Foco en el cumplimiento de objetivos' },
  { id: 'comp-8', nombre: 'Negociación Estratégica', descripcion: 'Acuerdos beneficiosos a mediano y largo plazo' },
];

let mockPuestosList: Puesto[] = [
  {
    id: 'pst-1',
    codigo: 'DEV-001',
    nombrePuesto: 'Desarrollador Full Stack Senior',
    empresaId: 'emp-1',
    empresaNombre: 'Tech Solutions S.A.',
    descripcion: 'Liderazgo técnico en desarrollo de plataformas web cloud-native',
    competencias: [
      { competenciaId: 'comp-1', nombre: 'Liderazgo y Gestión de Equipos', ponderacion: 8 },
      { competenciaId: 'comp-3', nombre: 'Pensamiento Analítico', ponderacion: 9 },
      { competenciaId: 'comp-6', nombre: 'Trabajo en Equipo', ponderacion: 8 },
    ],
    estado: 'ACTIVO',
    fechaCreacion: '2026-03-10',
  },
  {
    id: 'pst-2',
    codigo: 'ANA-002',
    nombrePuesto: 'Analista de Recursos Humanos',
    empresaId: 'emp-3',
    empresaNombre: 'Consultores Asociados',
    descripcion: 'Coordinación de procesos de selección y evaluación psicotécnica',
    competencias: [
      { competenciaId: 'comp-2', nombre: 'Resolución de Conflictos', ponderacion: 9 },
      { competenciaId: 'comp-4', nombre: 'Comunicación Asertiva', ponderacion: 10 },
    ],
    estado: 'ACTIVO',
    fechaCreacion: '2026-03-12',
  },
  {
    id: 'pst-3',
    codigo: 'GER-003',
    nombrePuesto: 'Gerente de Operaciones',
    empresaId: 'emp-4',
    empresaNombre: 'Logística Global',
    descripcion: 'Supervisión integral de supply chain y centros de distribución',
    competencias: [
      { competenciaId: 'comp-1', nombre: 'Liderazgo y Gestión de Equipos', ponderacion: 10 },
      { competenciaId: 'comp-7', nombre: 'Orientación a Resultados', ponderacion: 9 },
      { competenciaId: 'comp-8', nombre: 'Negociación Estratégica', ponderacion: 8 },
    ],
    estado: 'ACTIVO',
    fechaCreacion: '2026-03-15',
  },
];

export const puestoService = {
  async getEmpresas(): Promise<Empresa[]> {
    try {
      return await apiFetch<Empresa[]>('/empresas');
    } catch {
      return MOCK_EMPRESAS;
    }
  },

  async getCompetencias(): Promise<Competencia[]> {
    try {
      return await apiFetch<Competencia[]>('/competencias');
    } catch {
      return MOCK_COMPETENCIAS;
    }
  },

  async getPuestos(filters?: PuestosFilterParams): Promise<Puesto[]> {
    try {
      return await apiFetch<Puesto[]>('/puestos', { params: filters as Record<string, string> });
    } catch {
      let filtered = [...mockPuestosList];
      if (filters?.codigo) {
        filtered = filtered.filter((p) => p.codigo.toLowerCase().includes(filters.codigo!.toLowerCase()));
      }
      if (filters?.nombrePuesto) {
        filtered = filtered.filter((p) => p.nombrePuesto.toLowerCase().includes(filters.nombrePuesto!.toLowerCase()));
      }
      if (filters?.empresaId) {
        filtered = filtered.filter((p) => p.empresaId === filters.empresaId);
      }
      return filtered;
    }
  },

  async getPuestoById(id: string): Promise<Puesto | null> {
    try {
      return await apiFetch<Puesto>(`/puestos/${id}`);
    } catch {
      const found = mockPuestosList.find((p) => p.id === id);
      return found || null;
    }
  },

  async createPuesto(dto: CrearPuestoDTO): Promise<Puesto> {
    try {
      return await apiFetch<Puesto>('/puestos', {
        method: 'POST',
        body: JSON.stringify(dto),
      });
    } catch {
      const empresa = MOCK_EMPRESAS.find((e) => e.id === dto.empresaId);
      const newPuesto: Puesto = {
        id: `pst-${Date.now()}`,
        codigo: dto.codigo || `PST-${Math.floor(Math.random() * 900 + 100)}`,
        nombrePuesto: dto.nombrePuesto,
        empresaId: dto.empresaId,
        empresaNombre: empresa?.nombre || 'Empresa Asignada',
        descripcion: dto.descripcion,
        competencias: dto.competencias,
        estado: 'ACTIVO',
        fechaCreacion: new Date().toISOString().split('T')[0],
      };
      mockPuestosList = [newPuesto, ...mockPuestosList];
      return newPuesto;
    }
  },

  async deletePuesto(id: string): Promise<void> {
    try {
      await apiFetch<void>(`/puestos/${id}`, { method: 'DELETE' });
    } catch {
      mockPuestosList = mockPuestosList.filter((p) => p.id !== id);
    }
  },
};
