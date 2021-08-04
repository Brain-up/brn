import ApplicationSerializer from './application';

export default class UserWeeklyStatisticsSerializer extends ApplicationSerializer {
  primaryKey = 'date';
}
// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    userWeeklyStatistics: UserWeeklyStatisticsSerializer;
  }
}
