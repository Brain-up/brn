import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { MODES, type Mode } from 'brn/utils/task-modes';

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
      style={{html-safe (concat "visibility:" this.visibility ";")}}
      {{did-insert this.setLastMode @activeStep}}
      {{did-update this.setLastMode @activeStep}}
      ...attributes
    >
      <ExerciseSteps::Step
        @mode={{this.modeForListen}}
        {{on "click" (fn this.onClick this.MODES.LISTEN this.modeForListen)}}
      >
        {{t "control_exercises.listen"}}
      </ExerciseSteps::Step>
      <div class="chevron-container flex justify-center flex-1"><Ui::Chevron
          class="chevron"
        /></div>
      <ExerciseSteps::Step
        @mode={{this.modeForInteract}}
        {{on "click" (fn this.onClick this.MODES.INTERACT this.modeForInteract)}}
      >
        {{t "control_exercises.interact"}}
      </ExerciseSteps::Step>
      <div class="chevron-container flex justify-center flex-1"><Ui::Chevron
          class="chevron"
        /></div>
      <ExerciseSteps::Step
        @mode={{this.modeForTask}}
        {{on "click" (fn this.onClick this.MODES.TASK this.modeForTask)}}
      >
        {{t "control_exercises.solve"}}
      </ExerciseSteps::Step>
    </div>
  </template>
}
