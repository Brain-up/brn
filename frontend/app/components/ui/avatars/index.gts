import './index.css';
import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import { eq } from 'ember-truth-helpers';
import UiButton from 'brn/components/ui/button';

interface UiAvatarsSignature {
  Args: {
  selectedAvatar: string;
  onSubmit: (avatar: string) => void;
  onCancel: () => void;
  };
  Element: HTMLElement;
}

export default class UiAvatarsComponent extends Component<UiAvatarsSignature> {
  @tracked preferredAvatar!: string;

  @action storeCurrentAvatar() {
    this.preferredAvatar = this.args.selectedAvatar;
  }

  @action onSelect(avatar: string) {
    this.preferredAvatar = avatar;
  }

  @action onSubmit() {
    this.args.onSubmit(this.preferredAvatar);
  }

  get avatars() {
    return new Array(20).fill(0).map((_, index) => (index + 1).toString());
  }

  <template>
    <div
      {{didInsert this.storeCurrentAvatar}}
      class="p-4 sm:p-8 relative flex flex-row flex-wrap justify-between
       "
    >
      <div class="w-full mb-6">{{t "avatar_controls.title"}}</div>
      <div
        class="modal-max-h flex flex-row flex-wrap justify-around overflow-y-auto"
      >
        {{#each this.avatars as |avatar|}}
          <div class="w-32 h-32 m-2 rounded-full shadow-lg">
            <button
              title="Select Avatar"
              type="button"
              {{on "click" (fn this.onSelect avatar)}}
              class="btn-press {{if (eq avatar this.preferredAvatar) "activeTab"}}
                focus:outline-none"
                data-test-avatar-btn={{avatar}}
            >
              <img
                src="/pictures/avatars/avatar {{avatar}}.png"
                alt="Avatar Upload"
                class="object-none object-center w-auto h-full max-h-full mx-auto rounded-full"
              />
            </button>
          </div>
        {{/each}}
      </div>
      <div class="sm:justify-end flex justify-between w-full mt-8">
        <UiButton
          {{on "click" @onCancel}}
          data-test-submit-form
          @kind="outline"
          class="w-full text-lg"
          @title={{t "avatar_controls.cancel"}}
        />
    
        <UiButton
          @type="button"
          data-test-submit-form
          class="w-full text-lg"
          {{on "click" this.onSubmit}}
          @title={{t "avatar_controls.submit"}}
        />
      </div>
    </div>
  </template>
}
