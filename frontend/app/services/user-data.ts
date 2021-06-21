import Service, { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Router from '@ember/routing/router-service';
import NetworkService, { LatestUserDTO } from 'brn/services/network';
import IntlService from 'ember-intl/services/intl';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class UserDataService extends Service {
  @service('session') session!: Session;
  @service('router') router!: Router;
  @service('network') network!: NetworkService;
  @service('intl') intl!: IntlService;

  get userId() {
    return this.session.data?.user?.id;
  }
  get keyForAvatar() {
    return `user:${this.userId}:avatar_id`;
  }

  @tracked _selectedAvatarId = localStorage.getItem(this.keyForAvatar) || 1;

  get avatarUrl() {
    return `/pictures/avatars/avatar ${this.selectedAvatarId}.png`;
  }

  get selectedAvatarId() {
    return this.session.get('data.user.avatar') || this._selectedAvatarId;
  }
  set selectedAvatarId(value) {
    //debugger;
    localStorage.setItem(this.keyForAvatar, value.toString());
    this.network.patchUserInfo({
      avatar: value.toString(),
    } as LatestUserDTO);
    this.session.set('data.user.avatar', value);
    this.session.store.persist(this.session.data);
    this._selectedAvatarId = value;
  }

  @tracked selectedLocale: string | null = null;

  get user() {
    return this.session?.data?.user;
  }

  get activeLocale() {
    return this.selectedLocale || this.intl.primaryLocale;
  }

  shouldUpdateRoute() {
    return (
      this.router.currentRouteName !== 'description' &&
      this.router.currentRouteName !== 'profile.statistics' &&
      this.router.currentRouteName !== 'index'
    );
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
