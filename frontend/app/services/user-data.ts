import Service, { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Router from '@ember/routing/router-service';
import NetworkService, { UserDTO } from 'brn/services/network';
import IntlService from 'ember-intl/services/intl';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class UserDataService extends Service {
  @service('session') session!: Session;
  @service('router') router!: Router;
  @service('network') network!: NetworkService;
  @service('intl') intl!: IntlService;

  @tracked
  userModel!: UserDTO | undefined;

  get userAvatar(): string {
    return this.userModel?.avatar || '1';
  }

  get avatarUrl() {
    return `/pictures/avatars/avatar ${this.userAvatar}.png`;
  }

  @tracked selectedLocale: string | null = null;

  get user() {
    return this.session?.data?.user;
  }

  get activeLocale() {
    return this.selectedLocale || this.intl.primaryLocale;
  }

  get activeLocaleShort() {
    return this.activeLocale.split('-')[0];
  }

  shouldUpdateRoute() {
    const prefix = this.router.currentRouteName.split('.')[0];

    return prefix === 'groups' || prefix === 'group';
  }

  @action setLocale(localeName: string) {
    const name = localeName === 'ru' ? 'ru-ru' : 'en-us';
    this.intl.setLocale([name]);
    this.selectedLocale = name;
    localStorage.setItem('locale', name);

    if (this.shouldUpdateRoute()) {
      this.router.transitionTo('groups', { queryParams: { locale: name } });
    }
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  // eslint-disable-next-line no-unused-vars
  interface Registry {
    'user-data': UserDataService;
  }
}
