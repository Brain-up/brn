import DS from 'ember-data';

export default DS.JSONAPIAdapter.extend({
  coalesceFindRequests: true,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
});
