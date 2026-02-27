import { RESTAdapter } from '@warp-drive-mirror/legacy/adapter/rest';
import { inject as service } from '@ember/service';
import AuthTokenService from 'brn/services/auth-token';

export default class ApplicationAdapter extends RESTAdapter {
  @service('auth-token')
  authToken!: AuthTokenService;
  get token() {
    return this.authToken.token;
  }
  get headers() {
    return this.authToken.headers;
  }
  namespace = 'api';
  coalesceFindRequests = false;
  shouldReloadRecord() {
    return false;
  }
  shouldBackgroundReloadRecord() {
    return false;
  }

  urlForFindRecord(id: string, modelName: string, snapshot: any): string {
    let actualModelName = modelName;
    if (modelName.startsWith('task/')
    ) {
      actualModelName = 'tasks';
    }
    return super.urlForFindRecord(id, actualModelName, snapshot);
  }
}

