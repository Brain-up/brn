import DS from 'ember-data';

export default DS.RESTAdapter.extend({
  coalesceFindRequests: true,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
});
