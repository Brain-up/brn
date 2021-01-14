import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Ember from 'ember';
import Session from 'ember-simple-auth/services/session';
import IntlService from 'ember-intl/services/intl';

export default class ApplicationRoute extends Route {
  @service('session') session!: Session;
  @service('intl') intl!: IntlService;

  beforeModel() {
    const rawLocale = localStorage.getItem('locale');
    const locale = rawLocale === 'en-us' ? 'en-us' : 'ru-ru';
    this.intl.setLocale([locale]);
  }

  redirect(_: unknown, { to } : { to: { name: string }}) {
    if (Ember.testing) {
      // skip testing bahavour for now
      return;
    }
    if (to.name === 'user-agreement') {
      return;
    }
    if (!this.session.isAuthenticated) {
      this.replaceWith('login');
    }
  }
}
