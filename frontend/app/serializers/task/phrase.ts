import BaseTaskSerializer from '../task';

export default class TaskPhraseSerializer extends BaseTaskSerializer {}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/phrase': TaskPhraseSerializer;
  }
}
