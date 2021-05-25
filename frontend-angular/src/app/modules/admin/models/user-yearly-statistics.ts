import { UserExercisingProgressStatusType } from './user-exercising-progress-status';

export class UserYearlyStatistics {
  public date: string;
  public exercisingTimeSeconds: number;
  public progress: UserExercisingProgressStatusType;
  public exercisingDays: number;
}
