import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class HeaderComponent extends Component {
  @service('session') session;
  @service('router') router;
  @service('intl') intl;

  beforeModel() {
    this.intl.setLocale(['ru-ru']);
  }

  @action logout() {
    this.session.invalidate().then(() => {
      window.location.reload();
    });
  }
}
