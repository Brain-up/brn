import UserWeeklyStatisticsSerializer from './user-weekly-statistics';

export default class UserYearlyStatisticsSerializer extends UserWeeklyStatisticsSerializer {}
// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    userYearlyStatistics: UserYearlyStatisticsSerializer;
  }
}
