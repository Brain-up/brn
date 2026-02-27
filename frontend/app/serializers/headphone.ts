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

