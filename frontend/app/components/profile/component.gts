import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import Session from 'ember-simple-auth/services/session';
import UserDataService from 'brn/services/user-data';
import IntlService from 'ember-intl/services/intl';
import { UserDTO } from 'brn/services/network';
import { isBornYearValid, isNotEmptyString } from 'brn/utils/validators';
import { LinkTo } from '@ember/routing';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { concat } from '@ember/helper';
import { t } from 'ember-intl';
import { eq } from 'ember-truth-helpers';
import htmlSafe from 'brn/helpers/html-safe';
import ModalDialog from 'ember-modal-dialog/components/modal-dialog';
import UiAvatars from 'brn/components/ui/avatars';
import LoginFormInput from 'brn/components/login-form/input/component';

export default class ProfileComponent extends Component {
  @service('intl') intl!: IntlService;
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  @tracked showAvatarsModal = false;

  get avatarUrl() {
    return this.userData.avatarUrl;
  }

  get avatar() {
    return this.userData.userAvatar;
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

  @action onAvatarSubmit(selectedAvatar: string) {
    this.showAvatarsModal = false;
    this.userData.network.patchUserInfo({
      avatar: selectedAvatar,
    });
    this.userData.userModel = {
      ...(this.userData.userModel as UserDTO),
      avatar: selectedAvatar,
    };
  }

  @action onCancel() {
    this.showAvatarsModal = false;
  }

  @action onShowAvatars() {
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

  @action
  setBirthday(e: KeyboardEvent) {
      const key = e.key;
      const allowedKeys = [
        'Backspace',
        'Delete',
        'Tab',
        'Enter',
        'ArrowLeft',
        'ArrowRight',
      ];
      const isValid = allowedKeys.includes(key) || !isNaN(key as any);
      if (!isValid) {
        e.preventDefault();
      }
  }

  <template>
    {{#if this.showAvatarsModal}}
      <ModalDialog
        @overlayClass="p-8 z-50 min-h-full w-full fixed flex"
        @containerClass="max-w-4xl flex bg-white rounded-lg text-2xl m-auto"
      >
        <UiAvatars
          @selectedAvatar={{this.avatar}}
          @onCancel={{this.onCancel}}
          @onSubmit={{this.onAvatarSubmit}}
        />
      </ModalDialog>
    {{/if}}
    <section class="border-4 border-gray-100 rounded-md">
      <div
        class="bg-gradient-to-r from-blue-100 to-purple-100 profile lg:flex justify-between p-4"
      >
        <button
          type="button"
          title="{{this.user.email}}"
          style={{htmlSafe
            (concat "background-image: url('" this.avatarUrl "');")
          }} class="btn-press gradient-background focus:outline-none inline-flex items-center justify-center w-32 h-32 m-auto bg-center bg-contain border border-gray-400 rounded-full" {{on "click" this.onShowAvatars}}
        >
        </button>
      </div>
      <div class="sm:p-8 lg:p-12 p-4">
        <div class="mb-4">
          <LoginFormInput
            @model={{this.user}}
            @name="firstName"
            @label={{t "registration_form.name"}}
            @warning={{this.warningErrorFirstName}}
            @trimRight={{false}}
            {{on "change" (fn this.onInput "firstName")}}
          />
          <LoginFormInput
            @model={{this.user}}
            @name="lastName"
            @label={{false}}
            @warning={{this.warningErrorLastName}}
            @trimRight={{false}}
            {{on "change" (fn this.onInput "lastName")}}
          />
        </div>
        <div class="mb-4">
          <LoginFormInput
            required
            minlength="4"
            maxlength="4"
            pattern="[0-9]{4}"
            {{on "keydown" this.setBirthday}}
            {{on "change" (fn this.onInput "birthday")}}
            @warning={{this.warningErrorDate}}
            @model={{this.user}}
            @label={{t "registration_form.birthday"}}
            @name="birthday"
          />
    
        </div>
        <div class="mb-4">
          <p class="mb-2 text-sm font-bold text-gray-700">
            {{t "registration_form.gender"}}
          </p>
    
          <label class="ml-1 text-sm cursor-pointer" for="female">
            <input
              required
              name="gender"
              value="FEMALE"
              type="radio"
              checked={{eq this.user.gender "FEMALE"}} class="w-3 h-3 border-gray-300" id="female"
              {{on "change" (fn this.onInput "gender")}}
            />
            {{t "registration_form.gender_female"}}
          </label>
    
          <label class="ml-1 text-sm cursor-pointer" for="male">
            <input
              required
              name="gender"
              type="radio"
              value="MALE"
              checked={{eq this.user.gender "MALE"}} class="w-3 h-3 ml-4" id="male"
              {{on "change" (fn this.onInput "gender")}}
            />
            {{t "registration_form.gender_male"}}
          </label>
        </div>
    
        <div class="mb-4">
          <LoginFormInput
            disabled
            @model={{this.user}}
            @name="email"
            @label={{t "registration_form.email"}}
          />
        </div>
        <div class="mb-4">
          <LinkTo
            @route="password-recovery" class="hover:text-indigo-600 text-md focus:outline-none focus:underline font-medium text-indigo-500 transition duration-150 ease-in-out"
          >
            {{t "password_reset_form.update_password"}}
          </LinkTo>
        </div>
      </div>
    </section>
  </template>
}
