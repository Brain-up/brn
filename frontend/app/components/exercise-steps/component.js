import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { MODES } from 'brn/utils/task-modes';

const BUTTONS = {
    ACTIVE: 'active',
    ENABLED: 'enabled',
    DISABLED: 'disabled'
}

export default class ExerciseStepsComponent extends Component {
    @tracked modes = [];
    MODES = MODES
    get modeForListen() {
        if (this.args.activeStep === MODES.LISTEN) {
            return BUTTONS.ACTIVE;
        }
        return this.args.activeStep !== MODES.TASK ? BUTTONS.ENABLED: BUTTONS.DISABLED;
    }
    get modeForInteract() {
        if (this.args.activeStep === MODES.INTERACT) {
            return BUTTONS.ACTIVE;
        }
        return this.args.activeStep !== MODES.TASK ? BUTTONS.ENABLED: BUTTONS.DISABLED;
    }
    get modeForTask() {
        if (this.args.activeStep === MODES.TASK) {
            return BUTTONS.ACTIVE;
        }
        return this.modes.includes(MODES.LISTEN) && this.modes.includes(MODES.INTERACT) ? BUTTONS.ENABLED: BUTTONS.DISABLED;
    }
    @action onClick(key, mode) {
        if (mode === BUTTONS.DISABLED || mode === BUTTONS.ACTIVE) {
            return;
        }
        this.args.onClick(key);
    }
    @action setLastMode(_, [mode]) {
        if (mode === MODES.LISTEN) {
            this.modes = [mode];
        } else if (!this.modes.includes(mode)) {
            this.modes =  [...this.modes, mode];
        }
    }
}
