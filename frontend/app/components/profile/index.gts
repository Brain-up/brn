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
import NetworkService, { UserDTO } from 'brn/services/network';
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
import LoginFormInput from 'brn/components/login-form/input';
import UiConfirmDialog from 'brn/components/ui/confirm-dialog';
import type Store from 'brn/services/store';
import type { Headphone } from 'brn/schemas/headphone';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';

const HEADPHONE_TYPES = [
  { value: 'NOT_DEFINED', label: 'Not defined' },
  { value: 'ON_EAR_BLUETOOTH', label: 'On-ear Bluetooth' },
  { value: 'OVER_EAR_BLUETOOTH', label: 'Over-ear Bluetooth' },
  { value: 'IN_EAR_BLUETOOTH', label: 'In-ear Bluetooth' },
  { value: 'ON_EAR_NO_BLUETOOTH', label: 'On-ear Wired' },
  { value: 'OVER_EAR_NO_BLUETOOTH', label: 'Over-ear Wired' },
  { value: 'IN_EAR_NO_BLUETOOTH', label: 'In-ear Wired' },
] as const;

function headphoneTypeLabel(type: string): string {
  const found = HEADPHONE_TYPES.find((t) => t.value === type);
  return found ? found.label : type;
}

export default class ProfileComponent extends Component {
  @service('intl') intl!: IntlService;
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;
  @service('network') network!: NetworkService;
  @service('store') store!: Store;

  @tracked showAvatarsModal = false;
  @tracked showAddHeadphonesForm = false;
  @tracked headphones: Headphone[] = [];
  @tracked headphoneName = '';
  @tracked headphoneType = 'NOT_DEFINED';
  @tracked headphoneError = '';
  @tracked isLoadingHeadphones = false;
  @tracked headphonePendingDelete: Headphone | null = null;

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

  @action async onAvatarSubmit(selectedAvatar: string) {
    this.showAvatarsModal = false;
    this.userData.userModel = {
      ...(this.userData.userModel as UserDTO),
      avatar: selectedAvatar,
    };
    try {
      await this.network.updateAvatar(selectedAvatar);
    } catch (error) {
      console.error('Failed to update avatar:', error);
    }
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
      this.network.patchUserInfo({
        bornYear: parseInt(e.target.value, 10),
      });
    } else if (
      this.warningErrorFirstName === false &&
      fieldName === 'firstName'
    ) {
      this.network.patchUserInfo({
        name: `${e.target.value} ${this.userData.userModel.lastName}`,
      });
    } else if (
      this.warningErrorLastName === false &&
      fieldName === 'lastName'
    ) {
      this.network.patchUserInfo({
        name: `${this.userData.userModel.firstName} ${e.target.value}`,
      });
    } else if (fieldName === 'gender') {
      this.network.patchUserInfo({
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

  @action
  async loadHeadphones() {
    this.isLoadingHeadphones = true;
    try {
      this.headphones = await this.store.findAll<Headphone>('headphone');
    } catch (error) {
      console.error('Failed to load headphones:', error);
    }
    this.isLoadingHeadphones = false;
  }

  @action
  toggleAddHeadphonesForm() {
    this.showAddHeadphonesForm = !this.showAddHeadphonesForm;
    this.headphoneName = '';
    this.headphoneType = 'NOT_DEFINED';
    this.headphoneError = '';
  }

  @action
  onHeadphoneNameInput(e: Event & { target: HTMLInputElement }) {
    this.headphoneName = e.target.value;
    this.headphoneError = '';
  }

  @action
  onHeadphoneTypeChange(e: Event & { target: HTMLSelectElement }) {
    this.headphoneType = e.target.value;
  }

  @action
  async addHeadphones(e: Event) {
    e.preventDefault();
    const name = this.headphoneName.trim();
    if (!name) {
      this.headphoneError = this.intl.t('profile.headphones.name_required');
      return;
    }
    try {
      await this.network.addHeadphones({
        name,
        type: this.headphoneType,
        active: true,
      });
      this.showAddHeadphonesForm = false;
      this.headphoneName = '';
      this.headphoneType = 'NOT_DEFINED';
      this.headphoneError = '';
      await this.loadHeadphones();
    } catch (error: any) {
      this.headphoneError = error.message || 'Failed to add headphones';
    }
  }

  @action
  requestDeleteHeadphones(headphone: Headphone) {
    this.headphonePendingDelete = headphone;
  }

  @action
  cancelDeleteHeadphones() {
    this.headphonePendingDelete = null;
  }

  @action
  async confirmDeleteHeadphones() {
    const headphone = this.headphonePendingDelete;
    if (!headphone) return;
    this.headphonePendingDelete = null;
    try {
      await this.network.deleteHeadphones(String(headphone.id));
      await this.loadHeadphones();
    } catch (error) {
      console.error('Failed to delete headphones:', error);
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
          }} class="btn-press gradient-background focus:outline-hidden inline-flex items-center justify-center w-32 h-32 m-auto bg-center bg-contain border border-gray-400 rounded-full" {{on "click" this.onShowAvatars}}
        >
        </button>
      </div>
      <div class="sm:p-8 lg:p-12 p-4" {{didInsert this.loadHeadphones}}>
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

        <div class="mb-4" role="region" aria-label={{t "profile.headphones.title"}}>
          <p class="mb-2 text-sm font-bold text-gray-700">
            {{t "profile.headphones.title"}}
          </p>

          {{#if this.isLoadingHeadphones}}
            <div class="animate-pulse space-y-2">
              <div class="h-16 bg-gray-200 rounded"></div>
            </div>
          {{else}}
            {{#each this.headphones as |headphone|}}
              <div data-test-headphone-item class="flex items-center justify-between p-3 mb-2 bg-gray-50 border border-gray-200 rounded-lg">
                <div>
                  <p class="text-sm font-medium text-gray-800" data-test-headphone-name>{{headphone.name}}</p>
                  <p class="text-xs text-gray-500" data-test-headphone-type>{{headphoneTypeLabel headphone.type}}</p>
                </div>
                <button
                  data-test-delete-headphone
                  type="button"
                  aria-label={{t "profile.headphones.delete"}}
                  class="btn-press hover:text-red-700 hover:bg-red-100 min-w-[44px] min-h-[44px] p-2 text-red-500 rounded-full flex items-center justify-center"
                  title={{t "profile.headphones.delete"}}
                  {{on "click" (fn this.requestDeleteHeadphones headphone)}}
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            {{/each}}

            {{#if (eq this.headphones.length 0)}}
              <p class="text-sm text-gray-400 mb-2">{{t "profile.headphones.empty"}}</p>
            {{/if}}

            {{#if this.showAddHeadphonesForm}}
              <form data-test-add-headphones-form class="p-3 mt-2 bg-gray-50 border border-gray-200 rounded-lg" {{on "submit" this.addHeadphones}}>
                <div class="mb-2">
                  <label class="block mb-1 text-xs font-medium text-gray-600" for="headphone-name">
                    {{t "profile.headphones.name_label"}}
                  </label>
                  <input
                    data-test-headphone-name-input
                    id="headphone-name"
                    type="text"
                    value={{this.headphoneName}}
                    class="focus:ring-indigo-500 focus:border-indigo-500 block w-full px-3 py-2 text-sm border border-gray-300 rounded-md"
                    placeholder={{t "profile.headphones.name_placeholder"}}
                    {{on "input" this.onHeadphoneNameInput}}
                  />
                </div>
                <div class="mb-2">
                  <label class="block mb-1 text-xs font-medium text-gray-600" for="headphone-type">
                    {{t "profile.headphones.type_label"}}
                  </label>
                  <select
                    data-test-headphone-type-select
                    id="headphone-type"
                    class="focus:ring-indigo-500 focus:border-indigo-500 block w-full px-3 py-2 text-sm border border-gray-300 rounded-md"
                    {{on "change" this.onHeadphoneTypeChange}}
                  >
                    {{#each HEADPHONE_TYPES as |hType|}}
                      <option value={{hType.value}} selected={{eq this.headphoneType hType.value}}>{{hType.label}}</option>
                    {{/each}}
                  </select>
                </div>
                {{#if this.headphoneError}}
                  <p data-test-headphone-error class="mb-2 text-xs text-red-500">{{this.headphoneError}}</p>
                {{/if}}
                <div class="flex gap-2">
                  <button
                    data-test-submit-headphone
                    type="submit"
                    class="btn-press hover:bg-indigo-700 px-4 py-2 text-xs font-medium text-white bg-indigo-600 rounded-md"
                  >
                    {{t "profile.headphones.add_button"}}
                  </button>
                  <button
                    data-test-cancel-headphone
                    type="button"
                    class="btn-press hover:bg-gray-200 px-4 py-2 text-xs font-medium text-gray-700 bg-gray-100 rounded-md"
                    {{on "click" this.toggleAddHeadphonesForm}}
                  >
                    {{t "profile.headphones.cancel"}}
                  </button>
                </div>
              </form>
            {{else}}
              <button
                data-test-show-add-headphones
                type="button"
                class="btn-press hover:text-indigo-700 mt-1 text-sm font-medium text-indigo-500"
                {{on "click" this.toggleAddHeadphonesForm}}
              >
                + {{t "profile.headphones.add"}}
              </button>
            {{/if}}
          {{/if}}
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
            @route="password-recovery" class="hover:text-indigo-600 text-md focus:outline-hidden focus:underline font-medium text-indigo-500 transition duration-150 ease-in-out"
          >
            {{t "password_reset_form.update_password"}}
          </LinkTo>
        </div>
      </div>
    </section>
    {{#if this.headphonePendingDelete}}
      <UiConfirmDialog
        @message={{t "profile.headphones.confirm_delete"}}
        @onConfirm={{this.confirmDeleteHeadphones}}
        @onCancel={{this.cancelDeleteHeadphones}}
        @destructive={{true}}
      />
    {{/if}}
  </template>
}
