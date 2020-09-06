import Service from '@ember/service';
import fetch from 'fetch';
import { inject as service } from '@ember/service';
export default class NetworkService extends Service {
  @service('session') session;
  @service('store') store;
  prefix = '/api';
  get _headers() {
    return Object.assign(
      {
        'Content-Type': 'application/json',
      },
      this.store.adapterFor('application').headers,
    );
  }
  postRequest(entry, data) {
    return fetch(`${this.prefix}/${entry}`, {
      body: JSON.stringify(data),
      headers: this._headers,
      method: 'POST',
    });
  }
  request(entry) {
    return fetch(`${this.prefix}/${entry}`, {
      headers: this._headers,
      method: 'GET',
    });
  }
  async cloudUrl() {
    let result = await this.request('cloud/baseFileUrl');
    let { data } = await result.json();
    return data;
  }
  async getCurrentUser() {
    let result = await this.request('users/current');
    let { data } = await result.json();
    return Array.isArray(data) ? data[0] : data;
  }
  async loadCurrentUser() {
    const user = await this.getCurrentUser();
    user.initials = `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
    this.session.set('data.user', user);
  }
  createUser(user) {
    return this.postRequest('registration', user);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    'network': NetworkService;
  }
}
