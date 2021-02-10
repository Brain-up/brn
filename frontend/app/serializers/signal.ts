import ApplicationSerializer from './application';
import Model from '@ember-data/model';

export default class SignalSerializer extends ApplicationSerializer {
  payloadToTypeId(payload: { id: number }) {
    return { id: payload.id, type: 'signal' }
  }
  normalize(_: Model, rawPayload: any) {
    rawPayload.duration = rawPayload.length;
    const { id, type } = this.payloadToTypeId(rawPayload);
    return {
      id,
      type,
      attributes: { ...rawPayload }
    }
  }
}



// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'signal': SignalSerializer;
  }
}
