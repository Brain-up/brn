import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import Session from 'ember-simple-auth/services/session';
import UserDataService from 'brn/services/user-data';

export default class HeaderComponent extends Component {
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  get activeLocale() {
    return this.userData.activeLocale;
  }

  get avatarUrl() {
    return this.userData.avatarUrl;
  }

  get user() {
    return this.session?.data?.user;
  }

  @action logout() {
    this.session.invalidate().then(() => {
      window.location.reload();
    });
  }

  @action setLocale(localeName: string) {
    this.userData.setLocale(localeName);
  }
}
