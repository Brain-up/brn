import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import Router from '@ember/routing/router-service';
import Session from 'ember-simple-auth/services/session';
import IntlService from 'ember-intl/services/intl';

export default class HeaderComponent extends Component {
  @service('session') session!: Session;
  @service('router') router!: Router;
  @service('intl') intl!: IntlService;

  @tracked selectedLocale: string | null = null;

  @action logout() {
    this.session.invalidate().then(() => {
      window.location.reload();
    });
  }

  get user() {
    return this.session?.data?.user;
  }

  get activeLocale() {
    return this.selectedLocale || this.intl.primaryLocale;
  }

  @action setLocale(localeName: string) {
    const name = localeName === 'ru' ? 'ru-ru': 'en-us';
    this.intl.setLocale([name]);
    this.selectedLocale = name;
    localStorage.setItem('locale', name);
    this.router.transitionTo('groups', { queryParams: { locale: name } });
  }
}
