import DS from 'ember-data';

export default DS.RESTAdapter.extend({
  namespace: 'api',
  coalesceFindRequests: false,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
});
