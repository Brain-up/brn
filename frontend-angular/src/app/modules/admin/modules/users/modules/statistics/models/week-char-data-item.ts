import { UserExercisingProgressStatusType } from '@admin/models/user-exercising-progress-status';

export interface IWeekChartDataItem {
  x: string;
  y: number;
  progress: UserExercisingProgressStatusType;
}
