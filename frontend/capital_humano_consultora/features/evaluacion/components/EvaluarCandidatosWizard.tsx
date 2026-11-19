'use client';

import React from 'react';
import { Stepper } from '@/components/molecules/Stepper';
import { Button } from '@/components/ui/Button';
import { useEvaluacionWizard } from '../hooks/useEvaluacionWizard';
import { PasoCandidatos } from './PasoCandidatos';

const WIZARD_STEPS = [
  { id: 'candidatos', label: 'Candidatos' },
  { id: 'puesto', label: 'Puesto' },
  { id: 'claves', label: 'Claves' },
];

export const EvaluarCandidatosWizard: React.FC = () => {
  const wizard = useEvaluacionWizard();

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto pb-12">
      {/* Encabezado de la pantalla */}
      <header className="flex flex-col gap-1">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581] tracking-tight">
          Evaluar Candidatos
        </h1>
        <p className="text-sm text-[#757575]">
          Seleccione los candidatos, el puesto o función a evaluar y genere las claves de ingreso a los cuestionarios.
        </p>
      </header>

      {/* Indicador de pasos: 1 Candidatos, 2 Puesto, 3 Claves */}
      <Stepper
        steps={WIZARD_STEPS}
        currentStep={wizard.currentStep}
        onStepClick={wizard.handleStepClick}
      />

      {/* Paso 1: Búsqueda y selección de candidatos */}
      {wizard.currentStep === 0 && (
        <PasoCandidatos
          register={wizard.register}
          onBuscarSubmit={wizard.handleBuscarCandidatos}
          isSearching={wizard.isSearching}
          hasSearched={wizard.hasSearched}
          searchError={wizard.searchError}
          onDismissSearchError={wizard.handleDismissSearchError}
          resultadosBusqueda={wizard.resultadosBusqueda}
          selectedResultadoIds={wizard.selectedResultadoIds}
          onToggleResultado={wizard.handleToggleResultado}
          onToggleAllResultados={wizard.handleToggleAllResultados}
          onAgregar={wizard.handleAgregarCandidatos}
          candidatosAEvaluar={wizard.candidatosAEvaluar}
          selectedAEvaluarIds={wizard.selectedAEvaluarIds}
          onToggleAEvaluar={wizard.handleToggleAEvaluar}
          onToggleAllAEvaluar={wizard.handleToggleAllAEvaluar}
          onQuitar={wizard.handleQuitarCandidatos}
          step1Error={wizard.step1Error}
          blockedNros={wizard.blockedNros}
          onDismissStep1Error={wizard.handleDismissStep1Error}
          isValidating={wizard.isValidatingCandidatos}
          onSiguiente={wizard.handleSiguientePaso1}
        />
      )}

      {/* Paso 2 y Paso 3: se incorporan en los pasos siguientes */}
      {wizard.currentStep > 0 && (
        <div className="bg-white p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-4 items-start">
          <p className="text-sm text-[#757575]">
            Paso {wizard.currentStep + 1} en construcción.
          </p>
          <Button
            type="button"
            variant="secondary"
            leftIcon="arrow_back"
            onClick={() => wizard.setCurrentStep(0)}
          >
            Volver al Paso 1
          </Button>
        </div>
      )}
    </div>
  );
};

export default EvaluarCandidatosWizard;
