export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ON_HOLD' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'BLOCKER';

export const TASK_STATUSES: TaskStatus[] = [
  'TODO', 'IN_PROGRESS', 'DONE', 'ON_HOLD', 'CANCELLED'
];

export const TASK_PRIORITIES: TaskPriority[] = [
  'LOW', 'MEDIUM', 'HIGH', 'CRITICAL', 'BLOCKER'
];

export interface TaskDto {
  id?: string;
  title: string;
  description: string;
  taskStatusType: TaskStatus;
  taskPriorityType: TaskPriority;
  active: boolean;
  projectId: string;
  userId?: string;
}
