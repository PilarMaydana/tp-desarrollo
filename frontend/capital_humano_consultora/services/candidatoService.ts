import { Candidato, CandidatosFilterParams } from '@/types/candidato';
import { apiFetch } from './apiClient';

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
};
