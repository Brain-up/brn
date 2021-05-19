import { EUserExercisingProgressStatus } from './user-exercising-progress-status';

export class UserYearlyStatistics {
  public date: string;
  public exercisingTimeSeconds: number;
  public progress: EUserExercisingProgressStatus;
  public days: number;
}
