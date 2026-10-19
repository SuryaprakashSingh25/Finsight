export interface NavItem {
  label: string;
  icon: string;
  badge?: string;
}

export interface StatItem {
  label: string;
  value: string;
  detail: string;
  icon: string;
  color: 'teal' | 'blue' | 'purple';
}