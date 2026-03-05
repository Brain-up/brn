import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { MODES, type Mode } from 'brn/utils/task-modes';

const BUTTONS = {
  ACTIVE: 'active',
  ENABLED: 'enabled',
  DISABLED: 'disabled',
};

interface IExerciseStepsComponentArgs {
  activeStep: Mode;
  visible: boolean;
  onClick: (key: string) => unknown;
}

export default class ExerciseStepsComponent extends Component<IExerciseStepsComponentArgs> {
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
}
