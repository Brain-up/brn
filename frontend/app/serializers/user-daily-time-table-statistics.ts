import UserWeeklyStatisticsSerializer from "brn/serializers/user-weekly-statistics";

export default class UserDailyTimeTableStatisticsSerializer extends UserWeeklyStatisticsSerializer {
  primaryKey = 'seriesName';
}

