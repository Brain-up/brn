import ApplicationSerializer from './application';

export default class HeadphoneSerializer extends ApplicationSerializer {
  normalizeResponse() {
    const result = super.normalizeResponse(...arguments);
    return result;
  }
  serialize() {
    const result: Record<string, unknown> = super.serialize(...arguments);
    result['type'] = 'ON_EAR_BLUETOOTH';
    return result;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    headphone: HeadphoneSerializer;
  }
}
