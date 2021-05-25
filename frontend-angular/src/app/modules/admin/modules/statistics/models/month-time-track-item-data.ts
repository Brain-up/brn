import { UserExercisingProgressStatusType } from '@admin/models/user-exercising-progress-status';
import { Dayjs } from 'dayjs';

export interface IMonthTimeTrackItemData {
  progress: UserExercisingProgressStatusType;
  time: string;
  days: number;
  month: string;
  year: number;
  date: Dayjs;
}
