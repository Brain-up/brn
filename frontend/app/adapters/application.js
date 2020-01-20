import DS from 'ember-data';
import { computed } from '@ember/object';

export default DS.RESTAdapter.extend({
  namespace: 'api',
  coalesceFindRequests: false,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
  urlForFindRecord(id, modelName, snapshot) {
    let actualModelName = modelName;
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/words-sequences'
    ) {
      actualModelName = 'tasks';
    }
    return this._super(id, actualModelName, snapshot);
  },
  headers: computed(function() {
    return {
      Authorization: 'Basic YWRtaW46YWRtaW4=',
    };
  }),
});
