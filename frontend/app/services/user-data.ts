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

function isValidPlaybackRate(rate: number): boolean {
  return AUDIO_PLAYBACK_RATES.includes(
    rate as (typeof AUDIO_PLAYBACK_RATES)[number],
  );
}

function readStoredPlaybackRate(): number {
  let stored = '';
  try {
    stored = localStorage.getItem('audioPlaybackRate') ?? '';
  } catch (e) {
    // Storage access can throw (blocked site data, lockdown/webview modes).
    // This runs during service instantiation, so degrade to the default
    // rather than crashing every route that injects user-data.
    console.error('audioPlaybackRate: localStorage unavailable', e);
  }
  const raw = Number.parseFloat(stored);
  // Guard against corrupt/stale values; only accept one of the known rates.
  if (isValidPlaybackRate(raw)) {
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
    // Mirror the read-side validation: without it a NaN/0/arbitrary number
    // would drive playback for the whole session (rate 0 → Infinity timeout)
    // and only be discarded on the next reload.
    if (!isValidPlaybackRate(rate)) {
      console.error(`audioPlaybackRate: ignoring unsupported rate ${rate}`);
      return;
    }
    this._audioPlaybackRate = rate;
    try {
      localStorage.setItem('audioPlaybackRate', String(rate));
    } catch (e) {
      // The preference still applies for this session even if persisting fails.
      console.error('audioPlaybackRate: failed to persist', e);
    }
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
