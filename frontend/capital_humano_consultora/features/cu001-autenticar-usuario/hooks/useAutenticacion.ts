'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError } from '@/services/apiClient';
import {
  AuthResponseDTO,
  ConsultorLoginDTO,
  CandidatoLoginDTO,
  UserRole,
  ApiErrorDTO,
} from '../types';
import {
  ConsultorLoginSchema,
  CandidatoLoginSchema,
  ConsultorLoginFormValues,
  CandidatoLoginFormValues,
} from '../schemas/auth.schema';
import { cu001AuthService } from '../services/authService';
import { useAuth } from '@/context/AuthContext';
import { ROUTES } from '@/constants/routes';

export interface UseAutenticacionOptions {
  onSuccessConsultor?: (response: AuthResponseDTO) => void;
  onSuccessCandidato?: (response: AuthResponseDTO) => void;
}

export function useAutenticacion(options: UseAutenticacionOptions = {}) {
  const [activeRole, setActiveRole] = useState<UserRole>('CONSULTOR');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successInfo, setSuccessInfo] = useState<AuthResponseDTO | null>(null);
  const router = useRouter();
  const { saveAuthResponse } = useAuth();

  // Formulario Consultor
  const consultorForm = useForm<ConsultorLoginFormValues>({
    resolver: zodResolver(ConsultorLoginSchema),
    defaultValues: {
      usuario: '',
      contrasena: '',
    },
    mode: 'onTouched',
  });

  // Formulario Candidato
  const candidatoForm = useForm<CandidatoLoginFormValues>({
    resolver: zodResolver(CandidatoLoginSchema),
    defaultValues: {
      tipoDocumento: 'DNI',
      numeroDocumento: '',
      clave: '',
    },
    mode: 'onTouched',
  });

  /**
   * Cambia el rol activo en el SegmentedControl y limpia los errores y contraseñas
   */
  const handleRoleChange = (newRole: UserRole) => {
    setActiveRole(newRole);
    setErrorMessage(null);
    setSuccessInfo(null);
    consultorForm.setValue('contrasena', '');
    candidatoForm.setValue('clave', '');
  };

  /**
   * Cierra el banner de error (CU001 3.A.3.1.3 / 5.A.3)
   */
  const clearError = () => {
    setErrorMessage(null);
  };

  /**
   * Envío del formulario de Consultor
   */
  const submitConsultor = async (values: ConsultorLoginFormValues) => {
    setErrorMessage(null);
    setSuccessInfo(null);

    const dto: ConsultorLoginDTO = {
      usuario: values.usuario.trim().toUpperCase(),
      contrasena: values.contrasena,
    };

    try {
      const response = await cu001AuthService.loginConsultor(dto);
      saveAuthResponse(response, dto.usuario);
      setSuccessInfo(response);

      if (options.onSuccessConsultor) {
        options.onSuccessConsultor(response);
      } else {
        // Redirección por defecto para Consultor: pantalla principal CU009 (Dar de alta Puesto)
        router.push(ROUTES.CONSULTOR.PUESTOS_NUEVO);
      }
    } catch (err: unknown) {
      handleAuthError(err, 'CONSULTOR');
    }
  };

  /**
   * Envío del formulario de Candidato
   */
  const submitCandidato = async (values: CandidatoLoginFormValues) => {
    setErrorMessage(null);
    setSuccessInfo(null);

    const dto: CandidatoLoginDTO = {
      tipoDocumento: values.tipoDocumento,
      numeroDocumento: values.numeroDocumento.trim(),
      clave: values.clave.trim(),
    };

    try {
      const response = await cu001AuthService.loginCandidato(dto);
      saveAuthResponse(response, dto.numeroDocumento);
      setSuccessInfo(response);

      if (options.onSuccessCandidato) {
        options.onSuccessCandidato(response);
      } else {
        // Redirección para Candidato: acceso directo a su cuestionario (CU026)
        router.push(ROUTES.CANDIDATO.CUESTIONARIO);
      }
    } catch (err: unknown) {
      handleAuthError(err, 'CANDIDATO');
    }
  };

  /**
   * Manejo estandarizado de errores 401 y validación:
   * Muestra textualmente el campo 'mensaje' y blanquea el campo de contraseña.
   */
  const handleAuthError = (err: unknown, role: UserRole) => {
    let msg = 'Error al intentar iniciar sesión. Verifique los datos ingresados.';

    if (err instanceof ApiError) {
      const apiData = err.data as ApiErrorDTO | undefined;
      msg = apiData?.mensaje || err.message || msg;

      // Regla de Negocio: en 401 blanquear el campo de contraseña/clave
      if (err.status === 401) {
        if (role === 'CONSULTOR') {
          consultorForm.setValue('contrasena', '');
        } else {
          candidatoForm.setValue('clave', '');
        }
      }
    } else if (err instanceof Error) {
      msg = err.message;
    }

    setErrorMessage(msg);
  };

  return {
    activeRole,
    errorMessage,
    successInfo,
    consultorForm,
    candidatoForm,
    handleRoleChange,
    clearError,
    submitConsultor: consultorForm.handleSubmit(submitConsultor),
    submitCandidato: candidatoForm.handleSubmit(submitCandidato),
  };
}
