import Service from '@ember/service';
import fetch from 'fetch';
import { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Store from '@ember-data/store';
import UserDataService from './user-data';

export interface UserDTO {
  firstName: string;
  lastName: string;
  email: string;
  avatar: string;
  gender: 'MALE' | 'FEMALE';
  birthday: string;
  password?: string;
  id?: string;
}

export interface LatestUserDTO {
  name: string;
  email: string;
  password: string;
  gender: 'MALE' | 'FEMALE';
  bornYear: number;
  avatar: string;
  id?: string;
}

function fromLatestUserDto(user: LatestUserDTO): UserDTO {
  const [firstName = '', lastName = ''] = (user.name || '').split(' ');
  const bDate = new Date();

  bDate.setFullYear(user.bornYear);

  return {
    firstName: firstName || '',
    lastName: lastName || '',
    avatar: user.avatar,
    email: user.email,
    gender: user.gender,
    birthday: bDate.getFullYear().toString(),
    id: user.id as string,
  };
}

export default class NetworkService extends Service {
  @service('session') session!: Session;
  @service('user-data') userData?: UserDataService;
  @service('store') store!: Store;
  @service('router') router!: any;
  prefix = '/api';
  get token() {
    return this.store.adapterFor('application').token;
  }
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
  patch(entry: string, data: unknown) {
    return fetch(`${this.prefix}/${entry}`, {
      headers: this._headers,
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }
  async cloudUrl() {
    const result = await this.request('cloud/baseFileUrl');
    const { data } = await result.json();
    return data;
  }
  async getCurrentUser() {
    try {
      const result = await this.request('users/current');
      const { data } = await result.json();
      return fromLatestUserDto(Array.isArray(data) ? data[0] : data);
    } catch (e) {
      if (this.session.isAuthenticated) {
        await this.session.invalidate();
      }
      throw e;
    }
  }
  async patchUserInfo(
    userInfo: Partial<LatestUserDTO>,
  ): Promise<LatestUserDTO> {
    const result = await this.patch('users/current', userInfo);
    const { data } = await result.json();
    return data;
  }
  async loadCurrentUser() {
    try {
      const user: any = await this.getCurrentUser();
      user.initials = `${user.firstName.charAt(0)}${user.lastName.charAt(
        0,
      )}`.toUpperCase();
      this.userData.userModel = user;
    } catch (e) {
      this.router.transitionTo('login');
      const error = new Error('Unable to login');
      error.message = 'Unable to login';
      error.name = 'Unauthorized';
      error.code = 401;
      throw error;
    }
  }
  createUser(user: LatestUserDTO) {
    return this.postRequest('registration', user);
  }
  async subgroupStats(id: string) {
    const result = await this.request(`statistics/subgroups?ids=${id}`);
    const { data } = await result.json();
    return data[0];
  }
  async availableExercises(ids: string[]): Promise<string[]> {
    const result = await this.postRequest(`exercises/byIds`, {
      ids: ids.map((el) => parseInt(el, 10)),
    });
    const json = await result.json();
    const { data } = json;
    return data.map((el: number) => String(el));
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  // eslint-disable-next-line no-unused-vars
  interface Registry {
    network: NetworkService;
  }
}
