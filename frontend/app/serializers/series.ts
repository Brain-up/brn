import ApplicationSerializer from './application';
import Model from '@ember-data/model';

export default class SeriesSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({});
  normalize(typeClass: Model, rawPayload: any) {
    rawPayload.kind = rawPayload.type;
    return super.normalize(typeClass, rawPayload);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'series': SeriesSerializer;
  }
}
