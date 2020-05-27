import RESTAdapter from '@ember-data/adapter/rest';
import { inject as service } from '@ember/service';

export default class ApplicationAdapter extends RESTAdapter {
  @service('session')
  session;
  get headers() {
    if (!this.session.isAuthenticated) {
      return {};
    }
    return {
      Authorization: `Bearer ${this.session.data.authenticated.access_token}`,
    };
  }
  namespace = 'api';
  coalesceFindRequests = false;
  shouldReloadRecord() {
    return false;
  }
  shouldBackgroundReloadRecord() {
    return false;
  }
  urlForFindRecord(id, modelName, snapshot) {
    let actualModelName = modelName;
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/words-sequences' ||
      modelName === 'task/sentence' ||
      modelName === 'task/single-simple-words'
    ) {
      actualModelName = 'tasks';
    }
    return super.urlForFindRecord(id, actualModelName, snapshot);
  }
}
