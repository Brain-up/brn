import DS from 'ember-data';
import { computed } from '@ember/object';

export default DS.RESTAdapter.extend({
  headers: computed(function() {
    return {
      Authorization: 'Basic YWRtaW5AYWRtaW4uY29tOmFkbWlu',
    };
  }),
  namespace: 'api',
  coalesceFindRequests: false,
  shouldReloadRecord: () => false,
  shouldBackgroundReloadRecord: () => false,
  urlForFindRecord(id, modelName, snapshot) {
    let actualModelName = modelName;
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/words-sequences' ||
      modelName === 'task/sentence'
    ) {
      actualModelName = 'tasks';
    }
    return this._super(id, actualModelName, snapshot);
  },
});
