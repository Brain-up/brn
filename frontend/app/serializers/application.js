import DS from 'ember-data';
// import { singularize, pluralize } from 'ember-inflector';

export default DS.JSONSerializer.extend({
  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    const data = payload.data;
    return this._super(store, primaryModelClass, data, id, requestType);
  },
});
