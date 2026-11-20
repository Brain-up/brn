import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import UiHelp from 'brn/components/ui/help';
import UiInstructionsDialog from 'brn/components/ui/instructions-dialog';

interface ExercisePlaybackHelpSignature {
  Args: {
    triggerClass?: string;
  };
  Element: HTMLButtonElement;
}

export default class ExercisePlaybackHelpComponent extends Component<ExercisePlaybackHelpSignature> {
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
    this.triggerElement?.focus();
  }

  <template>
    <button
      data-test-playback-help-trigger
      type="button"
      aria-label={{t "instructions.trigger_aria"}}
      aria-haspopup="dialog"
      aria-expanded={{if this.isOpen "true" "false"}}
      aria-controls="instructions-dialog-title"
      class="btn-press inline-flex items-center justify-center min-h-[44px] min-w-[44px] rounded-full bg-white hover:bg-gray-100 shadow {{@triggerClass}}"
      {{on "click" this.open}}
      ...attributes
    >
      <UiHelp class="w-6 h-6" />
    </button>
    {{#if this.isOpen}}
      <UiInstructionsDialog @titleKey="instructions.playback_title" @onClose={{this.close}}>
        <p data-test-playback-help-body>
          {{t "instructions.playback_body" htmlSafe=true}}
        </p>
      </UiInstructionsDialog>
    {{/if}}
  </template>
}
