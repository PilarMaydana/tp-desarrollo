'use client';

import React from 'react';
import Link from 'next/link';
import { ROUTES } from '@/constants/routes';

export default function DevSandboxPage() {
  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-white rounded-xl shadow-md p-6 space-y-6">
        <div className="border-b pb-4">
          <span className="px-2.5 py-1 text-xs font-semibold uppercase tracking-wider bg-purple-100 text-[#3F2581] rounded-full">
            Entorno de Desarrollo Aislado (CDD)
          </span>
          <h1 className="text-2xl font-bold text-gray-800 mt-2">Dev Sandbox (/dev/sandbox)</h1>
          <p className="text-sm text-gray-600 mt-1">
            Espacio para montar y probar el feature / caso de uso actual de forma aislada antes de integrarlo al sistema oficial.
          </p>
        </div>

        <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
          <h2 className="font-semibold text-[#3F2581] mb-1">Montaje de Feature Actual</h2>
          <p className="text-sm text-gray-700">
            Aquí puedes importar y probar la pantalla o componente del Caso de Uso en desarrollo (ej. Alta de Puesto).
          </p>
        </div>

        <div className="pt-4 border-t">
          <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">
            Acceso Rápido a Prototipos
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
            <Link
              href={ROUTES.AUTH.LOGIN}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Login</span>
              <span className="text-xs text-gray-500">{ROUTES.AUTH.LOGIN}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.PUESTOS}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Gestionar Puestos</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.PUESTOS}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.PUESTOS_NUEVO}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Nuevo Puesto</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.PUESTOS_NUEVO}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.EVALUACIONES}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Evaluar Candidatos</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.EVALUACIONES}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.PREGUNTAS}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Gestión de Preguntas</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.PREGUNTAS}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.ORDEN_MERITOS}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Orden de Méritos</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.ORDEN_MERITOS}</span>
            </Link>
            <Link
              href={ROUTES.CONSULTOR.REPORTES}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Reportes</span>
              <span className="text-xs text-gray-500">{ROUTES.CONSULTOR.REPORTES}</span>
            </Link>
            <Link
              href={ROUTES.CANDIDATO.CUESTIONARIO}
              className="p-3 border rounded-lg hover:border-[#3F2581] hover:bg-purple-50/50 transition-colors flex items-center justify-between"
            >
              <span className="font-medium text-gray-800">Cuestionario Candidato</span>
              <span className="text-xs text-gray-500">{ROUTES.CANDIDATO.CUESTIONARIO}</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

