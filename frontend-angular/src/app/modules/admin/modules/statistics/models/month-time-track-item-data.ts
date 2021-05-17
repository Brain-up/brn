import { Dayjs } from 'dayjs';

export interface IMonthTimeTrackItemData {
  progress: number;
  time: string;
  days: number;
  month: string;
  year: number;
  date: Dayjs;
}
