import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { MODES, type Mode } from 'brn/utils/task-modes';
import htmlSafe from 'brn/helpers/html-safe';
import { concat } from '@ember/helper';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import ExerciseStepsStep from 'brn/components/exercise-steps/step';
import UiChevron from 'brn/components/ui/chevron';

const BUTTONS = {
  ACTIVE: 'active',
  ENABLED: 'enabled',
  DISABLED: 'disabled',
};

interface ExerciseStepsSignature {
  Args: {
  activeStep: Mode;
  visible: boolean;
  onClick: (key: string) => unknown;
  };
  Element: HTMLElement;
}

export default class ExerciseStepsComponent extends Component<ExerciseStepsSignature> {
  @tracked modes: Mode[] = [];
  MODES = MODES;
  get modeForListen() {
    if (this.args.activeStep === MODES.LISTEN) {
      return BUTTONS.ACTIVE;
    }
    return BUTTONS.ENABLED;
  }
  get modeForInteract() {
    if (this.args.activeStep === MODES.INTERACT) {
      return BUTTONS.ACTIVE;
    }
    return BUTTONS.ENABLED;
  }
  get modeForTask() {
    if (this.args.activeStep === MODES.TASK) {
      return BUTTONS.ACTIVE;
    }
    return this.modes.includes(MODES.LISTEN) &&
      this.modes.includes(MODES.INTERACT)
      ? BUTTONS.ENABLED
      : BUTTONS.DISABLED;
  }
  @action onClick(key: string, mode: string) {
    if (mode === BUTTONS.DISABLED || mode === BUTTONS.ACTIVE) {
      return;
    }
    this.args.onClick(key);
  }
  @action setLastMode(_: unknown, [mode]: Mode[]) {
    if (mode === MODES.LISTEN) {
      this.modes = [mode];
    } else if (!this.modes.includes(mode)) {
      this.modes = [...this.modes, mode];
    }
  }
  get visibility() {
    if (this.args.visible === false) {
      return 'hidden';
    } else {
      return 'visible';
    }
  }

  <template>
    <div
      style={{htmlSafe (concat "visibility:" this.visibility ";")}}
      {{didInsert this.setLastMode @activeStep}}
      {{didUpdate this.setLastMode @activeStep}}
      ...attributes
    >
      <ExerciseStepsStep
        @mode={{this.modeForListen}}
        {{on "click" (fn this.onClick this.MODES.LISTEN this.modeForListen)}}
      >
        {{t "control_exercises.listen"}}
      </ExerciseStepsStep>
      <div class="chevron-container flex justify-center flex-1"><UiChevron
          class="chevron"
        /></div>
      <ExerciseStepsStep
        @mode={{this.modeForInteract}}
        {{on "click" (fn this.onClick this.MODES.INTERACT this.modeForInteract)}}
      >
        {{t "control_exercises.interact"}}
      </ExerciseStepsStep>
      <div class="chevron-container flex justify-center flex-1"><UiChevron
          class="chevron"
        /></div>
      <ExerciseStepsStep
        @mode={{this.modeForTask}}
        {{on "click" (fn this.onClick this.MODES.TASK this.modeForTask)}}
      >
        {{t "control_exercises.solve"}}
      </ExerciseStepsStep>
    </div>
  </template>
}
