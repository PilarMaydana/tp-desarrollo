import { UserRole } from '@/types/auth';
import { IconName } from '@/components/ui/Icon';
import { ROUTES } from './routes';

export interface NavSubItem {
  label: string;
  href: string;
  enabled: boolean;
  icon?: IconName;
}

export interface NavLinkItem {
  type: 'link';
  label: string;
  href: string;
  icon: IconName;
  enabled: boolean;
  roles: UserRole[];
}

export interface NavGroupItem {
  type: 'group';
  label: string;
  icon: IconName;
  roles: UserRole[];
  children: NavSubItem[];
}

export type NavItem = NavLinkItem | NavGroupItem;

export const NAV_ITEMS: NavItem[] = [
  // Rutas Consultor
  {
    type: 'link',
    label: 'Evaluaciones',
    href: ROUTES.CONSULTOR.EVALUACIONES,
    icon: 'groups',
    enabled: true,
    roles: ['CONSULTOR'],
  },
  {
    type: 'group',
    label: 'Configurar evaluaciones',
    icon: 'tune',
    roles: ['CONSULTOR'],
    children: [
      {
        label: 'Competencias',
        href: ROUTES.CONSULTOR.COMPETENCIAS,
        enabled: true,
      },
      {
        label: 'Factores',
        // TODO apuntar al listado cuando exista el CU de gestión de factores
        href: ROUTES.CONSULTOR.FACTORES_NUEVO,
        enabled: true,
      },
      {
        label: 'Preguntas',
        href: ROUTES.CONSULTOR.PREGUNTAS,
        enabled: true,
      },
      {
        label: 'Opciones de respuesta',
        href: '/opciones-respuesta',
        enabled: false,
      },
    ],
  },
  {
    type: 'link',
    label: 'Reportes',
    href: ROUTES.CONSULTOR.REPORTES,
    icon: 'bar_chart',
    enabled: true,
    roles: ['CONSULTOR'],
  },
  {
    type: 'link',
    label: 'Orden de Méritos',
    href: ROUTES.CONSULTOR.ORDEN_MERITOS,
    icon: 'format_list_numbered',
    enabled: true,
    roles: ['CONSULTOR'],
  },
  {
    type: 'link',
    label: 'Puestos',
    href: ROUTES.CONSULTOR.PUESTOS_NUEVO,
    icon: 'work',
    enabled: true,
    roles: ['CONSULTOR'],
  },
  // Rutas Candidato
  {
    type: 'link',
    label: 'Mi cuestionario',
    href: ROUTES.CANDIDATO.CUESTIONARIO,
    icon: 'assignment',
    enabled: true,
    roles: ['CANDIDATO'],
  },
];
