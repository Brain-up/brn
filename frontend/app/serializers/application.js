import JSONSerializer from '@ember-data/serializer/json';

export default class ApplicationSerializer extends JSONSerializer {
  ATTR_NAMES_MAP = Object.freeze({});

  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    const data = payload.data;
    return super.normalizeResponse(store, primaryModelClass, data, id, requestType);
  }

  normalizeSingleResponse(store, primaryModelClass, payload, id, requestType) {
    const data = Array.isArray(payload) ? payload[0] : payload;
    return super.normalizeSingleResponse(store, primaryModelClass, data, id, requestType);
  }

  keyForAttribute(attrKey) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  }

  keyForRelationship(attrKey) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  }
}
