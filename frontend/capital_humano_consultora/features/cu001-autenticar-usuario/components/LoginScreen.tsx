'use client';

import React from 'react';
import { AuthLayout } from '@/components/layouts/AuthLayout';
import { SegmentedControl } from '@/components/molecules/SegmentedControl';
import { ErrorBanner } from './ErrorBanner';
import { ConsultorLoginForm } from './ConsultorLoginForm';
import { CandidatoLoginForm } from './CandidatoLoginForm';
import { useAutenticacion } from '../hooks/useAutenticacion';
import { AuthResponseDTO, UserRole } from '../types';

export interface LoginScreenProps {
  onSuccessConsultor?: (response: AuthResponseDTO) => void;
  onSuccessCandidato?: (response: AuthResponseDTO) => void;
  initialRole?: UserRole;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({
  onSuccessConsultor,
  onSuccessCandidato,
  initialRole,
}) => {
  const {
    activeRole,
    errorMessage,
    consultorForm,
    candidatoForm,
    handleRoleChange,
    clearError,
    submitConsultor,
    submitCandidato,
  } = useAutenticacion({
    onSuccessConsultor,
    onSuccessCandidato,
  });

  // Si se especificó un rol inicial y difiere del actual al montar
  React.useEffect(() => {
    if (initialRole && initialRole !== activeRole) {
      handleRoleChange(initialRole);
    }
  }, [initialRole]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <AuthLayout subtitle="Sistema de Evaluación de Perfiles Laborales">
      <div className="flex flex-col gap-6 w-full">
        {/* Selector de Rol: Consultor / Candidato */}
        <SegmentedControl value={activeRole} onChange={handleRoleChange} />

        {/* Banner de Error (con mensaje textual y botón de cierre) */}
        <ErrorBanner message={errorMessage} onClose={clearError} />

        {/* Formulario según el Rol Activo */}
        {activeRole === 'CONSULTOR' ? (
          <ConsultorLoginForm
            form={consultorForm}
            onSubmit={submitConsultor}
            isLoading={consultorForm.formState.isSubmitting}
          />
        ) : (
          <CandidatoLoginForm
            form={candidatoForm}
            onSubmit={submitCandidato}
            isLoading={candidatoForm.formState.isSubmitting}
          />
        )}
      </div>
    </AuthLayout>
  );
};

export default LoginScreen;
