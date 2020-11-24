import RESTAdapter from '@ember-data/adapter/rest';
import { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';

export default class ApplicationAdapter extends RESTAdapter {
  @service('session')
  session!: Session;
  get headers() {
    if (!this.session.isAuthenticated) {
      return {};
    }
    return {
      Authorization: `Basic ${this.session.data?.authenticated.access_token}`,
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
  urlForFindRecord(id: string, modelName: string, snapshot: any) {
    let actualModelName = modelName;
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/words-sequences' ||
      modelName === 'task/sentence' ||
      modelName === 'task/single-simple-words' ||
      modelName === 'task/phrase'
    ) {
      actualModelName = 'tasks';
    }
    return super.urlForFindRecord(id, actualModelName, snapshot);
  }
}



declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'application': ApplicationAdapter;
  }
}
