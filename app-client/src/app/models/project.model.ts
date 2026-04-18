export type ProjectStatus = 'PLANNING' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD' | 'CANCELLED';

export const PROJECT_STATUSES: ProjectStatus[] = [
  'PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED'
];

export interface ProjectDto {
  id?: string;
  title: string;
  description: string;
  projectStatusType: ProjectStatus;
  startDate: string;
  endDate: string;
  userId?: string;
}
