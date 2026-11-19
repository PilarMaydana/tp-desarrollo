'use client';

import React from 'react';
import { Stepper } from '@/components/molecules/Stepper';
import { useEvaluacionWizard } from '../hooks/useEvaluacionWizard';
import { PasoCandidatos } from './PasoCandidatos';
import { PasoPuesto } from './PasoPuesto';
import { PasoClavesConfirmacion } from './PasoClavesConfirmacion';
import { ModalCompetenciasNoEvaluables } from './ModalCompetenciasNoEvaluables';
import { ModalExportarExcel } from './ModalExportarExcel';

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

      {/* Paso 2: Selección de puesto y competencias requeridas */}
      {wizard.currentStep === 1 && (
        <PasoPuesto
          cantidadCandidatos={wizard.candidatosAEvaluar.length}
          puestos={wizard.puestos}
          isLoadingPuestos={wizard.isLoadingPuestos}
          puestosError={wizard.puestosError}
          onReintentarPuestos={wizard.cargarPuestos}
          selectedPuestoId={wizard.selectedPuestoId}
          onSelectPuesto={wizard.handleSelectPuesto}
          detallePuesto={wizard.detallePuesto}
          isLoadingCompetencias={wizard.isLoadingCompetencias}
          step2Error={wizard.step2Error}
          onDismissStep2Error={wizard.handleDismissStep2Error}
          isLoadingPreview={wizard.isLoadingPreview}
          onAnterior={wizard.handleAnteriorPaso2}
          onSiguiente={wizard.handleSiguientePaso2}
        />
      )}

      {/* Paso 3: Claves de ingreso y finalización */}
      {wizard.currentStep === 2 && wizard.previsualizacion && (
        <PasoClavesConfirmacion
          previsualizacion={wizard.previsualizacion}
          step3Error={wizard.step3Error}
          onDismissStep3Error={wizard.handleDismissStep3Error}
          isFinalizing={wizard.isFinalizing}
          isCreated={wizard.evaluacionCreadaId !== null}
          onAnterior={wizard.handleAnteriorPaso3}
          onCancelar={wizard.handleCancelarPaso3}
          onFinalizar={wizard.handleFinalizar}
        />
      )}

      {/* Modal Flujo 9.A: Competencias sin factores válidos */}
      <ModalCompetenciasNoEvaluables
        isOpen={wizard.modal9A.isOpen}
        mensaje={wizard.modal9A.mensaje}
        competencias={wizard.modal9A.competencias}
        onAceptar={wizard.handleAceptarModal9A}
      />

      {/* Modal Pasos 13-14 / 14.A: Evaluación creada y exportación a Excel */}
      <ModalExportarExcel
        isOpen={wizard.isExportModalOpen}
        isExporting={wizard.isExportingExcel}
        exportError={wizard.exportError}
        onDismissExportError={wizard.handleDismissExportError}
        onConfirmarSi={wizard.handleSiExportar}
        onRechazarNo={wizard.handleNoExportar}
      />
    </div>
  );
};

export default EvaluarCandidatosWizard;
