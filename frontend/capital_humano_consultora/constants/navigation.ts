import { UserRole } from '@/types/auth';
import { ROUTES } from './routes';

export interface NavItem {
  label: string;
  href: string;
  icon: string;
  roles: UserRole[];
}

export const NAV_ITEMS: NavItem[] = [
  // Rutas Consultor
  {
    label: 'Puestos',
    href: ROUTES.CONSULTOR.PUESTOS_NUEVO,
    icon: 'work',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Competencias',
    href: ROUTES.CONSULTOR.COMPETENCIAS,
    icon: 'check_square',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Factores',
    href: ROUTES.CONSULTOR.FACTORES_NUEVO,
    icon: 'puzzle',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Preguntas',
    href: ROUTES.CONSULTOR.PREGUNTAS,
    icon: 'file_question',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Evaluaciones',
    href: ROUTES.CONSULTOR.EVALUACIONES,
    icon: 'groups',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Orden de Méritos',
    href: ROUTES.CONSULTOR.ORDEN_MERITOS,
    icon: 'format_list_numbered',
    roles: ['CONSULTOR'],
  },
  {
    label: 'Reportes',
    href: ROUTES.CONSULTOR.REPORTES,
    icon: 'bar_chart',
    roles: ['CONSULTOR'],
  },
  // Rutas Candidato
  {
    label: 'Mi cuestionario',
    href: ROUTES.CANDIDATO.CUESTIONARIO,
    icon: 'assignment',
    roles: ['CANDIDATO'],
  },
];
