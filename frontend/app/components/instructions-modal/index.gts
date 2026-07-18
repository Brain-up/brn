import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import UiHelp from 'brn/components/ui/help';
import UiInstructionsDialog from 'brn/components/ui/instructions-dialog';

interface InstructionsModalSignature {
  Args: {
    triggerClass?: string;
  };
  Element: HTMLButtonElement;
}

export default class InstructionsModalComponent extends Component<InstructionsModalSignature> {
  @tracked isOpen = false;
  triggerElement: HTMLButtonElement | null = null;

  @action
  open(event: Event) {
    this.triggerElement = event.currentTarget as HTMLButtonElement;
    this.isOpen = true;
  }

  @action
  close() {
    this.isOpen = false;
    // Restore focus to the trigger so keyboard users return to where they
    // were before the dialog opened.
    this.triggerElement?.focus();
  }

  <template>
    <button
      data-test-instructions-trigger
      type="button"
      aria-label={{t "instructions.trigger_aria"}}
      aria-haspopup="dialog"
      aria-expanded={{if this.isOpen "true" "false"}}
      aria-controls="instructions-dialog-title"
      class="btn-press inline-flex items-center gap-1.5 min-h-[44px] px-3 py-2 text-white bg-white/10 hover:bg-white/20 rounded-full text-xs sm:text-sm font-semibold tracking-wider uppercase {{@triggerClass}}"
      {{on "click" this.open}}
      ...attributes
    >
      <UiHelp class="w-5 h-5" />
      <span>{{t "instructions.trigger_label"}}</span>
    </button>
    {{#if this.isOpen}}
      <UiInstructionsDialog @titleKey="instructions.title" @onClose={{this.close}}>
        <p data-test-instructions-intro class="mb-4">
          {{t "instructions.intro"}}
        </p>
        <ol class="space-y-5">
          <li>
            <h3 class="font-semibold text-gray-800 mb-1">
              {{t "instructions.step_1.heading"}}
            </h3>
            <p>{{t "instructions.step_1.body" htmlSafe=true}}</p>
          </li>
          <li>
            <h3 class="font-semibold text-gray-800 mb-1">
              {{t "instructions.step_2.heading"}}
            </h3>
            <p>{{t "instructions.step_2.body" htmlSafe=true}}</p>
          </li>
          <li>
            <h3 class="font-semibold text-gray-800 mb-1">
              {{t "instructions.step_3.heading"}}
            </h3>
            <p>{{t "instructions.step_3.body" htmlSafe=true}}</p>
          </li>
          <li>
            <h3 class="font-semibold text-gray-800 mb-1">
              {{t "instructions.step_4.heading"}}
            </h3>
            <p>{{t "instructions.step_4.body" htmlSafe=true}}</p>
          </li>
        </ol>
      </UiInstructionsDialog>
    {{/if}}
  </template>
}
