import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';

export default class HeaderComponent extends Component {
  @service('session') session;
  @service('router') router;
  @service('intl') intl;

  @tracked selectedLocale = null;

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

  @action setLocale(localeName) {
    const name = localeName === 'ru' ? 'ru-ru': 'en-us';
    this.intl.setLocale([name]);
    this.selectedLocale = name;
    localStorage.setItem('locale', name);
    this.router.transitionTo('groups.index', { queryParams: { reload: true } });
  }
}
