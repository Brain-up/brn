import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { BarOptionsType } from '@shared/components/bar-chart/models/bar-options';
import { UserExercisingProgressStatusType } from './user-exercising-progress-status';

export interface User {
  active: boolean;
  bornYear: number;
  diagnosticProgress: { SIGNALS: boolean };
  doneExercises: number;
  email: string;
  firstDone: string;
  gender: 'MALE' | 'FEMALE';
  id: number;
  isFavorite?: boolean;
  lastDone: string;
  lastWeek: {
    date: string;
    exercisingTimeSeconds: number;
    progress: UserExercisingProgressStatusType;
  }[];
  name: string;
  spentTime: number;
  studyDaysInCurrentMonth: number;
  userId: string;
}

export interface UserMapped extends User {
  age: number;
  currentWeekChart: {
    data: BarDataType;
    option: BarOptionsType;
  };
  progress: boolean;
}

export interface UserWithNoAnalytics {
  active: boolean;
  roles: string[];
  avatar: string;
  bornYear: number;
  changed: string;
  created: string;
  email: string;
  gender: string;
  headphones: any[];
  id: number;
  name: string;
  userId: string;
}
