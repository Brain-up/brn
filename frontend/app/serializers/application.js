import DS from 'ember-data';
// import { singularize, pluralize } from 'ember-inflector';

export default DS.JSONSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({}),

  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    const data = payload.data;
    return this._super(store, primaryModelClass, data, id, requestType);
  },

  normalizeSingleResponse(store, primaryModelClass, payload, id, requestType) {
    const data = Array.isArray(payload) ? payload[0] : payload;
    return this._super(store, primaryModelClass, data, id, requestType);
  },

  keyForAttribute(attrKey) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  },

  keyForRelationship(attrKey) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  },
});
