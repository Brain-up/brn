import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import Session from 'ember-simple-auth/services/session';
import UserDataService from 'brn/services/user-data';

export default class ProfileComponent extends Component {
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  @tracked showAvatarsModal = false;
  selectedAvatar: number = 0;

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
    this.selectedAvatar = id;
  }

  @action onAvatarSubmit() {
    this.showAvatarsModal = false;
    this.userData.selectedAvatarId = this.selectedAvatar;
  }

  @action onCancel() {
    this.showAvatarsModal = false;
  }

  @action onShowAvatars() {
    this.selectedAvatar = this.userData.selectedAvatarId;
    this.showAvatarsModal = true;
  }

  @action setLocale(localeName: string) {
    this.userData.setLocale(localeName);
  }
}
