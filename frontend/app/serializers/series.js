import ApplicationSerializer from './application';
export default class SeriesSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({});
  normalize(typeClass, rawPayload) {
    rawPayload.kind = rawPayload.type;
    return super.normalize(typeClass, rawPayload);
  }
}
