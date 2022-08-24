import UserWeeklyStatisticsSerializer from "brn/serializers/user-weekly-statistics";

export default class UserDailyTimeTableStatisticsSerializer extends UserWeeklyStatisticsSerializer {
  primaryKey = 'seriesName';
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    userDailyTimeTableStatisticsSerializer: UserDailyTimeTableStatisticsSerializer;
  }
}
