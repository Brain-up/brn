import Service, { service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import type RouterService from '@ember/routing/router-service';
import AuthTokenService from './auth-token';
import UserDataService from './user-data';
import { waitForPromise } from '@ember/test-waiters';
import { setCloudBaseUrl } from 'brn/utils/file-url';

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
  @service('auth-token') authToken!: AuthTokenService;
  @service('router') router!: RouterService;
  prefix = '/api';
  get token() {
    return this.authToken.token;
  }
  get _headers() {
    return Object.assign(
      {
        'Content-Type': 'application/json',
      },
      this.authToken.headers,
    );
  }
  postRequest(entry: string, data: unknown) {
    return waitForPromise(
      fetch(`${this.prefix}/${entry}`, {
        body: JSON.stringify(data),
        headers: this._headers,
        method: 'POST',
      }),
    );
  }
  request(entry: string) {
    return waitForPromise(
      fetch(`${this.prefix}/${entry}`, {
        headers: this._headers,
        method: 'GET',
      }),
    );
  }
  patch(entry: string, data: unknown) {
    return waitForPromise(
      fetch(`${this.prefix}/${entry}`, {
        headers: this._headers,
        method: 'PATCH',
        body: JSON.stringify(data),
      }),
    );
  }
  async cloudUrl() {
    const result = await this.request('cloud/baseFileUrl');
    const { data } = await result.json();
    return data;
  }
  async loadCloudUrl() {
    try {
      const url = await this.cloudUrl();
      if (url) {
        setCloudBaseUrl(url);
      }
    } catch (_e) {
      // Cloud URL is non-critical; fall back to relative paths
    }
  }
  async getCurrentUser() {
    try {
      const result = await this.request('users/current');
      const { data } = await result.json();
      const raw = Array.isArray(data) ? data[0] : data;
      const user = fromLatestUserDto(raw);
      // Store roles from the API response
      if (this.userData && raw.roles) {
        this.userData.roles = raw.roles;
      }
      return user;
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
    const json = await result.json();
    if (!result.ok) {
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to update user info',
      );
      error.errors = json.errors;
      throw error;
    }
    return json.data;
  }
  async loadCurrentUser() {
    try {
      const user = await this.getCurrentUser();
      (user as UserDTO & { initials?: string }).initials = `${user.firstName.charAt(0)}${user.lastName.charAt(
        0,
      )}`.toUpperCase();
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.userData!.userModel = user;
    } catch (_e) {
      this.router.transitionTo('login');
      const error: Error & { code?: number } = new Error('Unable to login');
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
  deleteRequest(entry: string) {
    return waitForPromise(
      fetch(`${this.prefix}/${entry}`, {
        headers: this._headers,
        method: 'DELETE',
      }),
    );
  }
  putRequest(entry: string, data?: unknown) {
    return waitForPromise(
      fetch(`${this.prefix}/${entry}`, {
        headers: this._headers,
        method: 'PUT',
        body: data !== undefined ? JSON.stringify(data) : undefined,
      }),
    );
  }
  async addHeadphones(data: { name: string; active?: boolean; type?: string; description?: string }) {
    const result = await this.postRequest('users/current/headphones', data);
    const json = await result.json();
    if (!result.ok) {
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to add headphones',
      );
      error.errors = json.errors;
      throw error;
    }
    return json.data;
  }
  async deleteHeadphones(id: string) {
    const result = await this.deleteRequest(`users/current/headphones/${id}`);
    if (!result.ok) {
      const json = await result.json();
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to delete headphones',
      );
      error.errors = json.errors;
      throw error;
    }
  }
  async updateAvatar(avatar: string) {
    const result = await this.putRequest(`users/current/avatar?avatar=${encodeURIComponent(avatar)}`);
    if (!result.ok) {
      const json = await result.json();
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to update avatar',
      );
      error.errors = json.errors;
      throw error;
    }
  }
  async getMonthHistories(month: number, year: number) {
    const result = await this.request(`study-history/monthHistories?month=${month}&year=${year}`);
    const json = await result.json();
    return json.data;
  }
  async getStudyHistoriesV2(from: string, to: string) {
    const result = await this.request(`v2/study-history/histories?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
    const json = await result.json();
    return json.data;
  }
  async userHasStatistics(userId: string): Promise<boolean> {
    const result = await this.request(`v2/study-history/user/${userId}/has/statistics`);
    const json = await result.json();
    return json.data;
  }
  async getDoctorPatients(doctorId: string) {
    const result = await this.request(`doctors/${doctorId}/patients`);
    const json = await result.json();
    return json.data;
  }
  async addPatient(doctorId: string, patientId: string) {
    const result = await this.postRequest(`doctors/${doctorId}/patients`, { id: patientId, type: 'PATIENT' });
    const json = await result.json();
    if (!result.ok) {
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to add patient',
      );
      error.errors = json.errors;
      throw error;
    }
    return json.data;
  }
  async removePatient(doctorId: string, patientId: string) {
    const result = await this.deleteRequest(`doctors/${doctorId}/patients/${patientId}`);
    if (!result.ok) {
      const json = await result.json();
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to remove patient',
      );
      error.errors = json.errors;
      throw error;
    }
  }
  uploadPictureFile(file: Blob, fileName: string): Promise<Response> {
    const formData = new FormData();
    formData.append('file', file, fileName);
    return waitForPromise(
      fetch(`${this.prefix}/cloud/upload/picture`, {
        body: formData,
        headers: this.authToken.headers,
        method: 'POST',
      }),
    );
  }
  async postAudiometryHistory(data: {
    audiometryTaskId: string;
    startTime: string;
    endTime?: string;
    executionSeconds: number;
    tasksCount: number;
    rightAnswers: number;
    headphones: string;
    sinAudiometryResults?: Record<number, number>;
  }) {
    const result = await this.postRequest('audiometry-history', data);
    const json = await result.json();
    if (!result.ok) {
      const error: Error & { errors?: string[] } = new Error(
        json.errors?.join(', ') ?? 'Failed to save audiometry history',
      );
      error.errors = json.errors;
      throw error;
    }
    return json.data;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  // eslint-disable-next-line no-unused-vars
  interface Registry {
    network: NetworkService;
  }
}
