import Service from '@ember/service';
import fetch from 'fetch';
export default class NetworkService extends Service {
  prefix = 'api';
  postRequest(entry, data) {
    return fetch(`${this.prefix}/${entry}`, {
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    });
  }

  createUser(user) {
    return this.postRequest('registration', user);
  }
}
