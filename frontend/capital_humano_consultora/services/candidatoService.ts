import {
  Candidato,
  CandidatosFilterParams,
  ImportacionCandidatosResponse,
  ErrorImportacionResponse,
} from '@/types/candidato';
import { apiFetch } from './apiClient';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const MOCK_CANDIDATOS: Candidato[] = [
  {
    id: 'c-1',
    nroCandidato: '1001',
    nombre: 'Agustín',
    apellido: 'Romero',
    tipoDocumento: 'DNI',
    nroDocumento: '39482103',
    email: 'agustin.romero@gmail.com',
  },
  {
    id: 'c-2',
    nroCandidato: '1002',
    nombre: 'Camila',
    apellido: 'Díaz',
    tipoDocumento: 'DNI',
    nroDocumento: '41209384',
    email: 'camila.diaz@hotmail.com',
  },
  {
    id: 'c-3',
    nroCandidato: '1003',
    nombre: 'Martín',
    apellido: 'Benítez',
    tipoDocumento: 'DNI',
    nroDocumento: '38192049',
    email: 'mbenitez@outlook.com',
  },
  {
    id: 'c-4',
    nroCandidato: '1004',
    nombre: 'Sofía',
    apellido: 'Alvarez',
    tipoDocumento: 'DNI',
    nroDocumento: '42918230',
    email: 'sofia.alvarez@gmail.com',
  },
];

export const candidatoService = {
  async getCandidatos(filters?: CandidatosFilterParams): Promise<Candidato[]> {
    try {
      return await apiFetch<Candidato[]>('/candidatos', { params: filters as Record<string, string> });
    } catch {
      let filtered = [...MOCK_CANDIDATOS];
      if (filters?.nroCandidato) {
        filtered = filtered.filter((c) => c.nroCandidato.includes(filters.nroCandidato!));
      }
      if (filters?.apellido) {
        filtered = filtered.filter((c) => c.apellido.toLowerCase().includes(filters.apellido!.toLowerCase()));
      }
      if (filters?.nombre) {
        filtered = filtered.filter((c) => c.nombre.toLowerCase().includes(filters.nombre!.toLowerCase()));
      }
      return filtered;
    }
  },

  importarCandidatos,
};

/**
 * Servicio para CU006: Importar Candidatos desde archivo CSV
 */
export async function importarCandidatos(
  file: File,
  token?: string
): Promise<ImportacionCandidatosResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const resolvedToken =
    token || (typeof window !== 'undefined' ? (sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token') || '') : '');

  const headers: Record<string, string> = {};
  if (resolvedToken) {
    headers['Authorization'] = `Bearer ${resolvedToken}`;
  }

  const endpoint = `${API_BASE_URL}/candidatos/importar`;

  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers,
      body: formData,
    });

    let data: unknown;
    try {
      data = await response.json();
    } catch {
      data = {
        mensaje: response.statusText || 'Error desconocido al procesar la respuesta del servidor.',
        errores: [],
      };
    }

    if (!response.ok) {
      throw data as ErrorImportacionResponse;
    }

    return data as ImportacionCandidatosResponse;
  } catch (error) {
    if (error && typeof error === 'object' && 'mensaje' in error) {
      throw error;
    }
    throw {
      mensaje:
        'No se pudo conectar con el servidor backend (http://localhost:8080). Verifique que el servicio esté activo.',
      errores: [],
    } as ErrorImportacionResponse;
  }
}
