import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import Session from 'ember-simple-auth/services/session';
import UserDataService from 'brn/services/user-data';
import IntlService from 'ember-intl/services/intl';
import { UserDTO } from 'brn/services/network';
import { isBornYearValid, isNotEmptyString } from 'brn/utils/validators';

export default class ProfileComponent extends Component {
  @service('intl') intl!: IntlService;
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  @tracked showAvatarsModal = false;
  @tracked selectedAvatar: string | number = 0;

  get avatarUrl() {
    return this.userData.avatarUrl;
  }

  get user() {
    return this.userData.userModel;
  }

  get activeLocale() {
    return this.userData.activeLocale;
  }

  get warningErrorDate() {
    const bYear = this.user?.birthday ?? '';

    if (isBornYearValid(bYear)) {
      return false;
    }

    return this.intl.t('registration_form.invalid_date');
  }

  get warningErrorFirstName() {
    const name = this.user?.firstName;

    if (isNotEmptyString(name)) {
      return false;
    }

    return this.intl.t('registration_form.empty_lastname');
  }

  get warningErrorLastName() {
    const name = this.user?.lastName;

    if (isNotEmptyString(name)) {
      return false;
    }

    return this.intl.t('registration_form.empty_lastname');
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

  @action onInput(
    fieldName: keyof UserDTO,
    e: Event & { target: HTMLInputElement },
  ) {
    this.userData.userModel = {
      ...(this.userData.userModel as UserDTO),
      [fieldName]: e.target.value,
    };

    if (this.warningErrorDate == false && fieldName === 'birthday') {
      this.userData.network.patchUserInfo({
        bornYear: parseInt(e.target.value, 10),
      });
    } else if (
      this.warningErrorFirstName === false &&
      fieldName === 'firstName'
    ) {
      this.userData.network.patchUserInfo({
        name: `${e.target.value} ${this.userData.userModel.lastName}`,
      });
    } else if (
      this.warningErrorLastName === false &&
      fieldName === 'lastName'
    ) {
      this.userData.network.patchUserInfo({
        name: `${this.userData.userModel.firstName} ${e.target.value}`,
      });
    } else if (fieldName === 'gender') {
      this.userData.network.patchUserInfo({
        gender: `${e.target.value}` as 'MALE' | 'FEMALE' | undefined,
      });
    }
  }
}
