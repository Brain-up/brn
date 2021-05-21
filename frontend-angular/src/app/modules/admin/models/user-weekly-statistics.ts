import { UserExercisingProgressStatusType } from './user-exercising-progress-status';

export class UserWeeklyStatistics {
  public date: string;
  public exercisingTimeSeconds: number;
  public progress: UserExercisingProgressStatusType;
}
