import { UserExercisingProgressStatusType } from '@admin/models/user-exercising-progress-status';

export interface ILastWeekChartDataItem {
  y: number;
  progress: UserExercisingProgressStatusType;
}
