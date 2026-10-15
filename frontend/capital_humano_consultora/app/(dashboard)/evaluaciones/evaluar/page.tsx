'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Stepper } from '@/components/molecules/Stepper';
import { SearchFilterBar } from '@/components/molecules/SearchFilterBar';
import { DualTransferList, TransferItem } from '@/components/organisms/DualTransferList';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { candidatoService } from '@/services/candidatoService';
import { puestoService } from '@/services/puestoService';
import { Candidato } from '@/types/candidato';
import { Puesto } from '@/types/puesto';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Select } from '@/components/ui/Select';
import { ROUTES } from '@/constants/routes';

const WIZARD_STEPS = [
  { id: 'step-1', label: 'Selección' },
  { id: 'step-2', label: 'Puesto' },
  { id: 'step-3', label: 'Finalizar' },
];

export default function EvaluarCandidatosPage() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(0);
  const [candidatos, setCandidatos] = useState<Candidato[]>([]);
  const [selectedCandidatos, setSelectedCandidatos] = useState<TransferItem[]>([]);
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [selectedPuestoId, setSelectedPuestoId] = useState<string>('');
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    candidatoService.getCandidatos().then(setCandidatos);
    puestoService.getPuestos().then(setPuestos);
  }, []);

  const handleSearchCandidatos = async (filters: Record<string, string>) => {
    const res = await candidatoService.getCandidatos({
      nroCandidato: filters.nroCandidato,
      apellido: filters.apellido,
      nombre: filters.nombre,
    });
    setCandidatos(res);
  };

  const availableItems: TransferItem[] = candidatos.map((c) => ({
    id: c.id,
    label: `${c.apellido}, ${c.nombre}`,
    subtitle: `${c.tipoDocumento} ${c.nroDocumento} - Nro: ${c.nroCandidato}`,
  }));

  const handleNext = () => {
    if (currentStep === 0 && selectedCandidatos.length === 0) {
      alert('Debe seleccionar al menos un candidato para continuar.');
      return;
    }
    if (currentStep === 1 && !selectedPuestoId) {
      alert('Debe seleccionar un puesto para realizar la evaluación.');
      return;
    }
    if (currentStep < WIZARD_STEPS.length - 1) {
      setCurrentStep((prev) => prev + 1);
    } else {
      setIsSuccess(true);
    }
  };

  const handleBack = () => {
    if (currentStep > 0) {
      setCurrentStep((prev) => prev - 1);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Evaluar Candidatos</h1>
        <p className="text-sm text-[#757575] mt-1">
          Flujo guiado para asignar evaluaciones y cuestionarios a candidatos postulados.
        </p>
      </div>

      {/* Stepper de Progreso */}
      <Stepper steps={WIZARD_STEPS} currentStep={currentStep} onStepClick={setCurrentStep} />

      {/* Paso 1: Selección de Candidatos */}
      {currentStep === 0 && (
        <div className="flex flex-col gap-6">
          <SearchFilterBar
            fields={[
              { key: 'nroCandidato', label: 'Nro. Candidato', placeholder: 'Ej: 1001' },
              { key: 'apellido', label: 'Apellido', placeholder: 'Ej: Romero' },
              { key: 'nombre', label: 'Nombre', placeholder: 'Ej: Agustín' },
            ]}
            onSearch={handleSearchCandidatos}
          />

          <DualTransferList
            availableTitle="Candidatos disponibles"
            selectedTitle="Candidatos a evaluar"
            availableItems={availableItems}
            selectedItems={selectedCandidatos}
            onChange={setSelectedCandidatos}
            withWeights={false}
          />
        </div>
      )}

      {/* Paso 2: Selección de Puesto */}
      {currentStep === 1 && (
        <div className="bg-white p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-6">
          <h2 className="text-xl font-bold text-[#3F2581]">Seleccionar Puesto para la Evaluación</h2>
          <p className="text-sm text-[#757575]">
            El cuestionario se generará en base a las competencias y ponderaciones del puesto seleccionado.
          </p>

          <NotchedField label="Puesto a evaluar" required>
            <Select
              options={puestos.map((p) => ({
                value: p.id,
                label: `${p.nombrePuesto} (${p.empresaNombre}) - Cód: ${p.codigo}`,
              }))}
              placeholder="Seleccione el puesto"
              value={selectedPuestoId}
              onChange={(e) => setSelectedPuestoId(e.target.value)}
            />
          </NotchedField>

          {selectedPuestoId && (
            <div className="p-4 bg-[#F4F0FF] rounded-xl border border-[#D0BCFF] mt-2">
              <h4 className="text-sm font-bold text-[#3F2581] mb-2">Competencias a evaluar:</h4>
              <div className="flex flex-wrap gap-2">
                {puestos
                  .find((p) => p.id === selectedPuestoId)
                  ?.competencias.map((comp) => (
                    <span
                      key={comp.competenciaId}
                      className="px-3 py-1 bg-white border border-[#3F2581]/30 rounded-full text-xs font-semibold text-[#3F2581]"
                    >
                      {comp.nombre || 'Competencia'} (Pond: {comp.ponderacion})
                    </span>
                  ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Paso 3: Confirmación / Finalizar */}
      {currentStep === 2 && (
        <div className="bg-white p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-6">
          {isSuccess ? (
            <div className="text-center py-8 flex flex-col items-center gap-3">
              <div className="w-16 h-16 rounded-full bg-[#E8F5E9] text-[#1B8354] flex items-center justify-center text-3xl font-bold">
                ✓
              </div>
              <h2 className="text-2xl font-bold text-[#1B8354]">
                ¡Evaluaciones Asignadas con Éxito!
              </h2>
              <p className="text-sm text-[#757575] max-w-md">
                Se enviaron las credenciales y el acceso a los cuestionarios correspondientes a los{' '}
                {selectedCandidatos.length} candidatos seleccionados.
              </p>
              <Button
                variant="primary"
                onClick={() => router.push(ROUTES.CONSULTOR.PUESTOS)}
                className="mt-4"
              >
                Volver a Puestos
              </Button>
            </div>
          ) : (
            <>
              <h2 className="text-xl font-bold text-[#3F2581]">Resumen de la Evaluación</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div className="p-4 bg-[#F4F0FF] rounded-xl">
                  <h4 className="font-bold text-[#3F2581] mb-1">Puesto:</h4>
                  <p className="text-[#333333]">
                    {puestos.find((p) => p.id === selectedPuestoId)?.nombrePuesto}
                  </p>
                </div>
                <div className="p-4 bg-[#F4F0FF] rounded-xl">
                  <h4 className="font-bold text-[#3F2581] mb-1">Candidatos a notificar:</h4>
                  <p className="text-[#333333]">{selectedCandidatos.length} postulantes</p>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {/* Barra de Acciones del Wizard */}
      {!isSuccess && (
        <ActionToolbar align="between">
          <Button
            type="button"
            variant="secondary"
            disabled={currentStep === 0}
            onClick={handleBack}
            leftIcon="arrow_back"
          >
            Anterior
          </Button>

          <Button
            type="button"
            variant="primary"
            onClick={handleNext}
            rightIcon={currentStep === WIZARD_STEPS.length - 1 ? 'check' : 'arrow_forward'}
          >
            {currentStep === WIZARD_STEPS.length - 1 ? 'Finalizar' : 'Siguiente'}
          </Button>
        </ActionToolbar>
      )}
    </div>
  );
}
