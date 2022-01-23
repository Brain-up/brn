import SingleSimpleWordsSerializer from './single-simple-words';


export default class TaskSingleWordsKorolevaSerializer extends SingleSimpleWordsSerializer {
}


// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/single-words-koroleva': TaskSingleWordsKorolevaSerializer;
  }
}
