import DS from 'ember-data';
import { computed } from '@ember/object';

export default DS.RESTAdapter.extend({
  namespace: 'api',
  coalesceFindRequests: false,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
  headers: computed(function() {
    return {
      Authorization: 'Basic YWRtaW46YWRtaW4=',
    };
  }),
});
