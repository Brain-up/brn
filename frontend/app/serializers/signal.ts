import ApplicationSerializer from './application';
import Model from '@warp-drive-mirror/legacy/model';

export default class SignalSerializer extends ApplicationSerializer {
  payloadToTypeId(payload: { id: number }) {
    return { id: payload.id, type: 'signal' };
  }
  normalize(_: Model, rawPayload: any) {
    rawPayload.duration = rawPayload.length;
    const { id, type } = this.payloadToTypeId(rawPayload);
    return {
      id,
      type,
      attributes: { ...rawPayload },
    };
  }
}
