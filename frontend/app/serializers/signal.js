import ApplicationSerializer from './application';

export default class SignalSerializer extends ApplicationSerializer {
  payloadToTypeId(payload) {
    return { id: payload.id, type: 'signal' }
  }
  normalize(typeClass, rawPayload) {
    rawPayload.duration = rawPayload.length;
    const { id, type } = this.payloadToTypeId(rawPayload);
    return {
      id,
      type,
      attributes: { ...rawPayload }
    }
  }
}
