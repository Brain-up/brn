import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import Session from 'ember-simple-auth/services/session';
import UserDataService from "brn/services/user-data";

export default class ProfileComponent extends Component {
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  @tracked showAvatarsModal = false;
  @tracked isActive = null;

  get avatarUrl() {
    return this.userData.avatarUrl;
  }

  get user() {
    return this.session?.data?.user;
  }

  get activeLocale() {
    return this.userData.activeLocale;
  }

  @action onAvatarSelect(id: number) {
    if (!id) {
      return;
    }
    this.isActive = id;
  }

  @action onAvatarSubmit() {
    this.userData.selectedAvatarId = this.isActive;
    this.showAvatarsModal = false;
  }

  @action onCancel() {
    this.showAvatarsModal = false;
  }

  @action onShowAvatars() {
    this.showAvatarsModal = true;
  }

  @action setLocale(localeName: string) {
    this.userData.setLocale(localeName)
  }
}
