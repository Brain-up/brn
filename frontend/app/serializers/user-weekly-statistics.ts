import ApplicationSerializer from './application';

export default class UserWeeklyStatisticsSerializer extends ApplicationSerializer {
  primaryKey = 'date';
}

