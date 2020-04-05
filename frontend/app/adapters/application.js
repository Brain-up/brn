import DS from 'ember-data';
import { inject as service } from '@ember/service';

export default DS.RESTAdapter.extend({
  session: service('session'),
  get headers() {
    if (!this.session.isAuthenticated) {
      return {};
    }
    return {
      Authorization: `Bearer ${this.session.data.authenticated.access_token}`,
    };
  },
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
