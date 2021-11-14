import { GenderType } from './gender';
import { UserExercisingProgressStatusType } from './user-exercising-progress-status';

export class User {
  public active: boolean;
  public bornYear: number;
  public diagnosticProgress: { SIGNALS: boolean };
  public email: string;
  public firstDone: string;
  public gender: GenderType;
  public id: number;
  public lastDone: string;
  public lastWeek: { exercisingTimeSeconds: number; progress: UserExercisingProgressStatusType }[];
  public name: string;
  public workDayByLastMonth: number;
  public isFavorite: boolean;
}
