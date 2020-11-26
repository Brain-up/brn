import Service from '@ember/service';
import fetch from 'fetch';
import { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Store from '@ember-data/store';
interface UserDTO {
  firstName: string;
  lastName: string;
  email: string;
  birthday: string;
  password: string;
}

export default class NetworkService extends Service {
  @service('session') session!: Session;
  @service('store') store!: Store;
  prefix = '/api';
  get _headers() {
    return Object.assign(
      {
        'Content-Type': 'application/json',
      },
      this.store.adapterFor('application').headers,
    );
  }
  postRequest(entry: string, data: unknown) {
    return fetch(`${this.prefix}/${entry}`, {
      body: JSON.stringify(data),
      headers: this._headers,
      method: 'POST',
    });
  }
  request(entry: string) {
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
  createUser(user: UserDTO) {
    return this.postRequest('registration', user);
  }
  async availableExercises(ids: string[]) {
    const result = await this.postRequest(`exercises/byIds`, {
      ids: ids.map((el)=>parseInt(el, 10))
    });
    const { data } = await result.json();
    return data.map((el: number)=>String(el));
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    'network': NetworkService;
  }
}
