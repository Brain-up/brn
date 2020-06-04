import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class HeaderComponent extends Component {
  @service('session') session;
  @service('router') router;
  @service('intl') intl;

  @action logout() {
    this.session.invalidate().then(() => {
      window.location.reload();
    });
  }

  @action setLocale(localeName) {
    const name = localeName === 'ru' ? 'ru-ru': 'en-us';
    this.intl.setLocale([name]);
    this.router.transitionTo('groups.index', { queryParams: { reload: true } });
  }
}
