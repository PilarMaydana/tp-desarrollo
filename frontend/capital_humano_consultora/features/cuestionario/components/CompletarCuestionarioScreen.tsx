'use client';

import React from 'react';
import { useCuestionario } from '../hooks/useCuestionario';
import { EstadoCuestionarioScreen } from './EstadoCuestionarioScreen';
import { InstruccionesCuestionario } from './InstruccionesCuestionario';
import { ModalTiempoAgotado } from './ModalTiempoAgotado';
import { WizardBloqueCuestionario } from './WizardBloqueCuestionario';

export interface CompletarCuestionarioScreenProps {
  cuestionarioId: string | number;
}

/**
 * Pantalla principal del CU 026 "Completar Cuestionario" (vista del CANDIDATO).
 * Orquesta las fases: cargando -> instrucciones -> wizard -> finalizado,
 * más estados de no encontrado / no disponible / error de conexión.
 */
export const CompletarCuestionarioScreen: React.FC<
  CompletarCuestionarioScreenProps
> = ({ cuestionarioId }) => {
  const state = useCuestionario(cuestionarioId);

  if (
    state.fase === 'cargando' ||
    state.fase === 'no_encontrado' ||
    state.fase === 'no_disponible' ||
    state.fase === 'error_conexion' ||
    state.fase === 'finalizado'
  ) {
    return (
      <EstadoCuestionarioScreen
        fase={state.fase}
        mensajeEstado={state.mensajeEstado}
        motivoFinalizacion={state.motivoFinalizacion}
        mensajeFinalizacion={state.mensajeFinalizacion}
        onReintentar={
          state.fase === 'error_conexion' ? state.handleReintentarCarga : undefined
        }
        onSalir={state.handleSalir}
      />
    );
  }

  if (state.fase === 'instrucciones' && state.instruccionesData) {
    return (
      <InstruccionesCuestionario
        datos={state.instruccionesData}
        isStarting={state.isStarting}
        errorPanel={state.errorPanel}
        onDismissError={state.handleDismissErrorPanel}
        onAceptar={state.handleAceptarInstrucciones}
        onCancelar={state.handleSalir}
      />
    );
  }

  if (state.fase === 'wizard' && state.bloqueActual) {
    return (
      <>
        <WizardBloqueCuestionario
          nombrePuesto={state.nombrePuesto}
          bloqueActual={state.bloqueActual}
          totalBloques={state.totalBloques}
          preguntasOrdenadas={state.preguntasOrdenadas}
          esUltimoBloque={state.esUltimoBloque}
          segundosRestantes={state.segundosRestantes}
          tiempoFormateado={state.tiempoFormateado}
          respuestasSeleccionadas={state.respuestasSeleccionadas}
          preguntasFaltantesIds={state.preguntasFaltantesIds}
          errorPanel={state.errorPanel}
          isSubmitting={state.isSubmitting}
          tiempoAgotado={state.tiempoAgotado}
          onSeleccionarOpcion={state.handleSeleccionarOpcion}
          onEnviarBloque={state.handleEnviarBloque}
          onDismissError={state.handleDismissErrorPanel}
        />

        <ModalTiempoAgotado
          isOpen={state.mostrarModalTiempoAgotado}
          onAceptar={state.handleCerrarModalTiempoAgotado}
        />
      </>
    );
  }

  return null;
};

export default CompletarCuestionarioScreen;
