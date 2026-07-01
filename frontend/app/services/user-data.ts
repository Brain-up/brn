import Service, { service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Router from '@ember/routing/router-service';
import NetworkService, { UserDTO } from 'brn/services/network';
import IntlService from 'ember-intl/services/intl';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

// Selectable speech-playback speeds, slowest → fastest. 1 is the natural rate.
export const AUDIO_PLAYBACK_RATES = [0.5, 0.75, 1, 1.25, 1.5] as const;
const DEFAULT_AUDIO_PLAYBACK_RATE = 1;

function readStoredPlaybackRate(): number {
  const raw = Number.parseFloat(localStorage.getItem('audioPlaybackRate') ?? '');
  // Guard against corrupt/stale values; only accept one of the known rates.
  if (AUDIO_PLAYBACK_RATES.includes(raw as (typeof AUDIO_PLAYBACK_RATES)[number])) {
    return raw;
  }
  return DEFAULT_AUDIO_PLAYBACK_RATE;
}

export default class UserDataService extends Service {
  @service('session') session!: Session;
  @service('router') router!: Router;
  @service('network') network!: NetworkService;
  @service('intl') intl!: IntlService;

  @tracked
  userModel!: UserDTO | undefined;

  @tracked roles: string[] = [];

  get isSpecialist(): boolean {
    return this.roles.some((r) => r === 'SPECIALIST' || r === 'ROLE_SPECIALIST');
  }

  get isAdmin(): boolean {
    return this.roles.some((r) => r === 'ADMIN' || r === 'ROLE_ADMIN');
  }

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
    const prefix = this.router.currentRouteName?.split('.')[0];

    return prefix === 'groups' || prefix === 'group';
  }

  @tracked _audioPlaybackRate: number = readStoredPlaybackRate();

  get audioPlaybackRate(): number {
    return this._audioPlaybackRate;
  }

  @action setAudioPlaybackRate(rate: number) {
    this._audioPlaybackRate = rate;
    localStorage.setItem('audioPlaybackRate', String(rate));
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
