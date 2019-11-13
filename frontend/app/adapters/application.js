import DS from 'ember-data';
import ENV from 'brn/config/environment';

export default DS.RESTAdapter.extend({
  host: ENV.BRN.API_HOST,
  coalesceFindRequests: false,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
});
