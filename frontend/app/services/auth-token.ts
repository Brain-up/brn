import Service from '@ember/service';
import { service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';

export default class AuthTokenService extends Service {
  @service('session') session!: Session;

  get token(): string {
    return (
      this.session.data?.authenticated?.user?.stsTokenManager?.accessToken ?? ''
    );
  }

  get headers(): Record<string, string> {
    if (!this.session.isAuthenticated) {
      return {};
    }
    return {
      Authorization: `Bearer ${this.token}`,
    };
  }
}

declare module '@ember/service' {
  interface Registry {
    'auth-token': AuthTokenService;
  }
}
