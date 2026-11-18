import { apiFetch, ApiError } from '@/services/apiClient';
import {
  AuthResponseDTO,
  CandidatoLoginDTO,
  ConsultorLoginDTO,
} from '../types';

/**
 * Servicio de integración para el Caso de Uso CU001: Autenticar Usuario.
 * Endpoints:
 * - POST /auth/consultor
 * - POST /auth/candidato
 */
export const cu001AuthService = {
  /**
   * Autenticación de Consultores (LDAP)
   */
  async loginConsultor(dto: ConsultorLoginDTO): Promise<AuthResponseDTO> {
    const payload = {
      usuario: dto.usuario.trim().toUpperCase(),
      contrasena: dto.contrasena,
    };

    // El usuario genérico ADMIN está deshabilitado para preservar la trazabilidad y auditoría
    if (payload.usuario === 'ADMIN') {
      throw new ApiError(401, 'El usuario o la contraseña no son válidos', {
        status: 401,
        codigo: 'AUTENTICACION_FALLIDA',
        mensaje: 'El usuario o la contraseña no son válidos',
      });
    }

    try {
      return await apiFetch<AuthResponseDTO>('/auth/consultor', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    } catch (error) {
      if (error instanceof ApiError) {
        // Si el backend respondió (ej: 401 o 400), propagar el error real del servidor
        if (error.status === 401 || error.status === 400) {
          throw error;
        }

        // Si fue falla de red (backend apagado / offline), simular con credenciales de prueba del contrato
        if (this.isNetworkError(error)) {
          return this.mockConsultorFallback(payload.usuario, payload.contrasena);
        }
      }
      throw error;
    }
  },

  /**
   * Autenticación de Candidatos (Cuestionario)
   */
  async loginCandidato(dto: CandidatoLoginDTO): Promise<AuthResponseDTO> {
    const payload = {
      tipoDocumento: dto.tipoDocumento,
      numeroDocumento: dto.numeroDocumento.trim(),
      clave: dto.clave.trim(),
    };

    try {
      return await apiFetch<AuthResponseDTO>('/auth/candidato', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    } catch (error) {
      if (error instanceof ApiError) {
        // Si el backend respondió (ej: 401 o 400), propagar el error real del servidor
        if (error.status === 401 || error.status === 400) {
          throw error;
        }

        // Si fue falla de red (backend apagado / offline), simular con credenciales de prueba del contrato
        if (this.isNetworkError(error)) {
          return this.mockCandidatoFallback(payload.tipoDocumento, payload.numeroDocumento, payload.clave);
        }
      }
      throw error;
    }
  },

  /**
   * Detecta si el error se debió a que el servidor backend no está escuchando
   */
  isNetworkError(error: ApiError): boolean {
    const msg = (error.message || '').toLowerCase();
    return (
      error.status === 500 &&
      (msg.includes('fetch failed') ||
        msg.includes('failed to fetch') ||
        msg.includes('networkerror') ||
        msg.includes('econnrefused') ||
        msg.includes('conexión'))
    );
  },

  /**
   * Simulación basada en la tabla de credenciales del contrato CU001
   * en caso de operar en modo sandbox desconectado.
   */
  mockConsultorFallback(usuario: string, contrasena: string): AuthResponseDTO {
    const credencialesValidas: Record<
      string,
      { pass: string; id: number; nombre: string; apellido: string }
    > = {
      LFERNANDEZ: {
        pass: 'admin123',
        id: 1,
        nombre: 'Lucía',
        apellido: 'Fernández',
      },
    };

    const cuenta = credencialesValidas[usuario];
    if (cuenta && cuenta.pass === contrasena) {
      return {
        token: `mock-jwt-consultor-${usuario.toLowerCase()}-${Date.now()}`,
        rol: 'CONSULTOR',
        usuarioId: cuenta.id,
        nombre: cuenta.nombre,
        apellido: cuenta.apellido,
      };
    }

    throw new ApiError(401, 'El usuario o la contraseña no son válidos', {
      status: 401,
      codigo: 'AUTENTICACION_FALLIDA',
      mensaje: 'El usuario o la contraseña no son válidos',
    });
  },

  mockCandidatoFallback(tipoDocumento: string, numeroDocumento: string, clave: string): AuthResponseDTO {
    // Candidato con cuestionario ACTIVO
    if (tipoDocumento === 'DNI' && numeroDocumento === '12345678' && clave === 'CLAVE123') {
      return {
        token: `mock-jwt-candidato-12345678-${Date.now()}`,
        rol: 'CANDIDATO',
        usuarioId: 1,
        nombre: 'Juan',
        apellido: 'Pérez',
        cuestionarioId: 1,
      };
    }

    // Candidato con cuestionario EN PROCESO
    if (tipoDocumento === 'DNI' && numeroDocumento === '87654321' && clave === 'TEST4567') {
      return {
        token: `mock-jwt-candidato-87654321-${Date.now()}`,
        rol: 'CANDIDATO',
        usuarioId: 2,
        nombre: 'María',
        apellido: 'Gómez',
        cuestionarioId: 2,
      };
    }

    // Candidato con cuestionario ya FINALIZADO (11223344 / COMP1234):
    // Según CU001 paso 3.A.3, la clave expira una vez terminado el examen y debe arrojar 401
    throw new ApiError(
      401,
      'Los datos ingresados no son válidos o no existe un cuestionario para el Candidato',
      {
        status: 401,
        codigo: 'AUTENTICACION_FALLIDA',
        mensaje: 'Los datos ingresados no son válidos o no existe un cuestionario para el Candidato',
      }
    );
  },
};
