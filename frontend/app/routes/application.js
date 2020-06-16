import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Ember from 'ember';
export default class ApplicationRoute extends Route {
  @service('session') session;
  @service('intl') intl;

  beforeModel() {
    const rawLocale = localStorage.getItem('locale');
    const locale = rawLocale === 'en-us' ? 'en-us' : 'ru-ru';
    this.intl.setLocale([locale]);
  }

  redirect() {
    if (Ember.testing) {
      // skip testing bahavour for now
      return;
    }
    if (!this.session.isAuthenticated) {
      this.replaceWith('login');
    }
  }
}
