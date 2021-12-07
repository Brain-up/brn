import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { BarOptionsType } from '@shared/components/bar-chart/models/bar-options';
import { UserExercisingProgressStatusType } from './user-exercising-progress-status';

export class User {
  public active: boolean;
  public bornYear: number;
  public diagnosticProgress: { SIGNALS: boolean };
  public email: string;
  public firstDone: string;
  public gender: 'MALE' | 'FEMALE';
  public id: number;
  public isFavorite: boolean;
  public lastDone: string;
  public lastWeek: {
    exercisingTimeSeconds: number;
    progress: UserExercisingProgressStatusType;
  }[];
  public name: string;
  public workDayByLastMonth: number;
}

export class UserMapped extends User {
  age: number;
  currentWeekChart: {
    data: BarDataType;
    option: BarOptionsType;
  };
}
