import Component from '@glimmer/component';
import { service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { on } from '@ember/modifier';
import { fn, concat } from '@ember/helper';
import { t } from 'ember-intl';
import { eq } from 'ember-truth-helpers';
import type IntlService from 'ember-intl/services/intl';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type { Headphone } from 'brn/schemas/headphone';
import UiButton from 'brn/components/ui/button';
import UiConfirmDialog from 'brn/components/ui/confirm-dialog';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';

const HEADPHONE_TYPES = [
  'NOT_DEFINED',
  'ON_EAR_BLUETOOTH',
  'OVER_EAR_BLUETOOTH',
  'IN_EAR_BLUETOOTH',
  'ON_EAR_NO_BLUETOOTH',
  'OVER_EAR_NO_BLUETOOTH',
  'IN_EAR_NO_BLUETOOTH',
] as const;

export default class UiHeadphonesComponent extends Component {
  @service('intl') intl!: IntlService;
  @service('network') network!: NetworkService;
  @service('store') store!: Store;

  @tracked headphones: Headphone[] = [];
  @tracked headphoneName = '';
  @tracked headphoneType: string = 'NOT_DEFINED';
  @tracked headphoneError = '';
  @tracked isLoading = false;
  @tracked showAddForm = false;
  @tracked pendingDelete: Headphone | null = null;

  @action
  async loadHeadphones() {
    this.isLoading = true;
    try {
      this.headphones = await this.store.findAll<Headphone>('headphone');
    } catch (error) {
      console.error('Failed to load headphones:', error);
    }
    this.isLoading = false;
  }

  @action
  toggleAddForm() {
    this.showAddForm = !this.showAddForm;
    this.headphoneName = '';
    this.headphoneType = 'NOT_DEFINED';
    this.headphoneError = '';
  }

  @action
  onNameInput(e: Event & { target: HTMLInputElement }) {
    this.headphoneName = e.target.value;
    this.headphoneError = '';
  }

  @action
  onTypeChange(e: Event & { target: HTMLSelectElement }) {
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
      this.showAddForm = false;
      this.headphoneName = '';
      this.headphoneType = 'NOT_DEFINED';
      this.headphoneError = '';
      await this.loadHeadphones();
    } catch (error: any) {
      this.headphoneError = error.message || 'Failed to add headphones';
    }
  }

  @action
  requestDelete(headphone: Headphone) {
    this.pendingDelete = headphone;
  }

  @action
  cancelDelete() {
    this.pendingDelete = null;
  }

  @action
  async confirmDelete() {
    const headphone = this.pendingDelete;
    if (!headphone) return;
    this.pendingDelete = null;
    try {
      await this.network.deleteHeadphones(String(headphone.id));
      await this.loadHeadphones();
    } catch (error) {
      console.error('Failed to delete headphones:', error);
    }
  }

  <template>
    <div role="region" aria-label={{t "profile.headphones.title"}} {{didInsert this.loadHeadphones}}>
      <p class="mb-2 text-sm font-bold text-gray-700">
        {{t "profile.headphones.title"}}
      </p>

      {{#if this.isLoading}}
        <div class="animate-pulse space-y-2">
          <div class="h-16 bg-gray-200 rounded"></div>
        </div>
      {{else}}
        {{#each this.headphones as |headphone|}}
          <div data-test-headphone-item class="flex items-center justify-between p-3 mb-2 bg-gray-50 border border-gray-200 rounded-lg">
            <div>
              <p class="text-sm font-medium text-gray-800" data-test-headphone-name>{{headphone.name}}</p>
              <p class="text-xs text-gray-500" data-test-headphone-type>
                {{t (concat "profile.headphones.types." headphone.type)}}
              </p>
            </div>
            <button
              data-test-delete-headphone
              type="button"
              aria-label={{t "profile.headphones.delete"}}
              class="btn-press hover:text-red-700 hover:bg-red-100 min-w-[44px] min-h-[44px] p-2 text-red-500 rounded-full flex items-center justify-center"
              title={{t "profile.headphones.delete"}}
              {{on "click" (fn this.requestDelete headphone)}}
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

        {{#if this.showAddForm}}
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
                {{on "input" this.onNameInput}}
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
                {{on "change" this.onTypeChange}}
              >
                {{#each HEADPHONE_TYPES as |hType|}}
                  <option value={{hType}} selected={{eq this.headphoneType hType}}>
                    {{t (concat "profile.headphones.types." hType)}}
                  </option>
                {{/each}}
              </select>
            </div>
            {{#if this.headphoneError}}
              <p data-test-headphone-error class="mb-2 text-xs text-red-500">{{this.headphoneError}}</p>
            {{/if}}
            <div class="flex gap-2">
              <UiButton
                data-test-submit-headphone
                @type="submit"
                @kind="primary"
                @size="small"
                @title={{t "profile.headphones.add_button"}}
              />
              <UiButton
                data-test-cancel-headphone
                @type="button"
                @kind="outline"
                @size="small"
                @title={{t "profile.headphones.cancel"}}
                {{on "click" this.toggleAddForm}}
              />
            </div>
          </form>
        {{else}}
          <button
            data-test-show-add-headphones
            type="button"
            class="btn-press hover:text-indigo-700 mt-1 text-sm font-medium text-indigo-500"
            {{on "click" this.toggleAddForm}}
          >
            + {{t "profile.headphones.add"}}
          </button>
        {{/if}}
      {{/if}}
    </div>

    {{#if this.pendingDelete}}
      <UiConfirmDialog
        @message={{t "profile.headphones.confirm_delete"}}
        @onConfirm={{this.confirmDelete}}
        @onCancel={{this.cancelDelete}}
        @destructive={{true}}
      />
    {{/if}}
  </template>
}
