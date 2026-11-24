'use client';

import React from 'react';
import {
  Menu,
  Users,
  User,
  Bell,
  LogOut,
  Search,
  ArrowLeft,
  ArrowRight,
  Eye,
  EyeOff,
  Edit2,
  Lock,
  AtSign,
  Briefcase,
  ClipboardList,
  BarChart3,
  HelpCircle,
  ListOrdered,
  Plus,
  Trash2,
  Check,
  CheckSquare,
  Puzzle,
  SlidersVertical,
  ChevronDown,
  ChevronRight,
  Filter,
  X,
  Sparkles,
  FileQuestion,
  Timer,
  Clock,
  Upload,
  CheckCircle,
  AlertCircle,
  FileText,
  UserPlus,
  Copy,
  Table,
  ClipboardCheck,
  UserCheck,
  Printer,
  LucideProps,
} from 'lucide-react';

export type IconName =
  | 'menu'
  | 'groups'
  | 'person'
  | 'notifications'
  | 'logout'
  | 'search'
  | 'arrow_back'
  | 'arrow_forward'
  | 'visibility'
  | 'visibility_off'
  | 'edit'
  | 'lock'
  | 'alternate_email'
  | 'work'
  | 'assignment'
  | 'file_question'
  | 'bar_chart'
  | 'help_outline'
  | 'format_list_numbered'
  | 'plus'
  | 'trash'
  | 'check'
  | 'check_square'
  | 'puzzle'
  | 'tune'
  | 'chevron_down'
  | 'chevron_right'
  | 'filter'
  | 'close'
  | 'sparkles'
  | 'auto_awesome'
  | 'timer'
  | 'schedule'
  | 'clock'
  | 'upload'
  | 'group_add'
  | 'check_circle'
  | 'error'
  | 'file_text'
  | 'copy'
  | 'table'
  | 'clipboard_check'
  | 'user_check'
  | 'printer';

interface IconProps extends Omit<LucideProps, 'ref'> {
  name: IconName | string;
  size?: number | string;
  className?: string;
}

const ICON_MAP: Record<string, React.FC<LucideProps>> = {
  menu: Menu,
  groups: Users,
  person: User,
  notifications: Bell,
  logout: LogOut,
  search: Search,
  arrow_back: ArrowLeft,
  arrow_forward: ArrowRight,
  visibility: Eye,
  visibility_off: EyeOff,
  edit: Edit2,
  lock: Lock,
  alternate_email: AtSign,
  work: Briefcase,
  assignment: ClipboardList,
  file_question: FileQuestion,
  bar_chart: BarChart3,
  help_outline: HelpCircle,
  format_list_numbered: ListOrdered,
  plus: Plus,
  trash: Trash2,
  check: Check,
  check_square: CheckSquare,
  puzzle: Puzzle,
  tune: SlidersVertical,
  chevron_down: ChevronDown,
  chevron_right: ChevronRight,
  filter: Filter,
  close: X,
  sparkles: Sparkles,
  auto_awesome: Sparkles,
  timer: Timer,
  schedule: Clock,
  clock: Clock,
  upload: Upload,
  group_add: UserPlus,
  check_circle: CheckCircle,
  error: AlertCircle,
  file_text: FileText,
  copy: Copy,
  table: Table,
  clipboard_check: ClipboardCheck,
  user_check: UserCheck,
  printer: Printer,
};

export const Icon: React.FC<IconProps> = ({ name, size = 20, className = '', ...props }) => {
  const Component = ICON_MAP[name] || HelpCircle;
  return <Component size={size} className={className} {...props} />;
};

export default Icon;
