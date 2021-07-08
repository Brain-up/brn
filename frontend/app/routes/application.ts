import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Ember from 'ember';
import Session from 'ember-simple-auth/services/session';
import IntlService from 'ember-intl/services/intl';
import type Transition from '@ember/routing/-private/transition';

export default class ApplicationRoute extends Route {
  @service('session') session!: Session;
  @service('intl') intl!: IntlService;

  beforeModel() {
    const rawLocale = localStorage.getItem('locale');
    const locale = rawLocale === 'en-us' ? 'en-us' : 'ru-ru';
    this.intl.setLocale([locale]);
  }

  redirect(_: unknown, { to }: Transition) {
    if (Ember.testing) {
      // skip testing bahavour for now
      return;
    }
    if (['user-agreement', 'description'].includes(to.name)) {
      return;
    }
    if (!this.session.isAuthenticated) {
      this.replaceWith('index');
    }
  }
}
