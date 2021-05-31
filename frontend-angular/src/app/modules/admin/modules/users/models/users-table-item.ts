import { GenderType } from '@admin/models/gender';
import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { BarOptionsType } from '@shared/components/bar-chart/models/bar-options';

export interface IUsersTableItem {
  id: number;
  name: string;
  yearsOld: number;
  gender: Lowercase<GenderType>;
  firstVisit: {
    date: string;
    time: string;
  };
  lastVisit: {
    date: string;
    time: string;
  };
  lastWeek: {
    data: BarDataType;
    option: BarOptionsType;
  };
  workingDaysInLastMonth: number;
  hasProgress: boolean;
  isFavorite: boolean;
}
