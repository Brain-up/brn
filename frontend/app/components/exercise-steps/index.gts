import './index.css';
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

const BUTTONS = {
  ACTIVE: 'active' as const,
  ENABLED: 'enabled' as const,
  DISABLED: 'disabled' as const,
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

  get isListenCompleted() {
    return this.modes.includes(MODES.LISTEN) && this.args.activeStep !== MODES.LISTEN;
  }
  get isInteractCompleted() {
    return this.modes.includes(MODES.INTERACT) && this.args.activeStep !== MODES.INTERACT;
  }

  get listenStepClass() {
    const classes = ['exercise-step'];
    if (this.modeForListen === BUTTONS.ACTIVE) classes.push('exercise-step--active');
    if (this.isListenCompleted) classes.push('exercise-step--completed');
    return classes.join(' ');
  }
  get interactStepClass() {
    const classes = ['exercise-step'];
    if (this.modeForInteract === BUTTONS.ACTIVE) classes.push('exercise-step--active');
    if (this.isInteractCompleted) classes.push('exercise-step--completed');
    if (this.isListenCompleted && this.modeForInteract !== BUTTONS.ACTIVE) {
      classes.push('exercise-step--next');
    }
    return classes.join(' ');
  }
  get taskStepClass() {
    const classes = ['exercise-step'];
    if (this.modeForTask === BUTTONS.ACTIVE) classes.push('exercise-step--active');
    if (this.modeForTask === BUTTONS.DISABLED) classes.push('exercise-step--locked');
    if (this.isInteractCompleted && this.modeForTask !== BUTTONS.ACTIVE && this.modeForTask !== BUTTONS.DISABLED) {
      classes.push('exercise-step--next');
    }
    return classes.join(' ');
  }

  get connector1Class() {
    return this.isListenCompleted ? 'exercise-connector exercise-connector--filled' : 'exercise-connector';
  }
  get connector2Class() {
    return this.isInteractCompleted ? 'exercise-connector exercise-connector--filled' : 'exercise-connector';
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
      class="c-exercise-steps"
      style={{htmlSafe (concat "visibility:" this.visibility ";")}}
      {{didInsert this.setLastMode @activeStep}}
      {{didUpdate this.setLastMode @activeStep}}
      ...attributes
    >
      {{!-- Step 1: Listen --}}
      <div class={{this.listenStepClass}}>
        <div class="exercise-step__indicator">
          {{#if this.isListenCompleted}}
            <svg class="exercise-step__check" viewBox="0 0 16 16" fill="none"><path d="M3 8.5L6.5 12L13 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{else}}
            <span class="exercise-step__number">1</span>
          {{/if}}
        </div>
        <ExerciseStepsStep
          @mode={{this.modeForListen}}
          {{on "click" (fn this.onClick this.MODES.LISTEN this.modeForListen)}}
        >
          {{t "control_exercises.listen"}}
        </ExerciseStepsStep>
      </div>

      {{!-- Connector 1 --}}
      <div class={{this.connector1Class}}>
        <div class="exercise-connector__line"></div>
      </div>

      {{!-- Step 2: Interact --}}
      <div class={{this.interactStepClass}}>
        <div class="exercise-step__indicator">
          {{#if this.isInteractCompleted}}
            <svg class="exercise-step__check" viewBox="0 0 16 16" fill="none"><path d="M3 8.5L6.5 12L13 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{else}}
            <span class="exercise-step__number">2</span>
          {{/if}}
        </div>
        <ExerciseStepsStep
          @mode={{this.modeForInteract}}
          {{on "click" (fn this.onClick this.MODES.INTERACT this.modeForInteract)}}
        >
          {{t "control_exercises.interact"}}
        </ExerciseStepsStep>
      </div>

      {{!-- Connector 2 --}}
      <div class={{this.connector2Class}}>
        <div class="exercise-connector__line"></div>
      </div>

      {{!-- Step 3: Solve --}}
      <div class={{this.taskStepClass}}>
        <div class="exercise-step__indicator">
          <span class="exercise-step__number">3</span>
        </div>
        <ExerciseStepsStep
          @mode={{this.modeForTask}}
          {{on "click" (fn this.onClick this.MODES.TASK this.modeForTask)}}
        >
          {{t "control_exercises.solve"}}
        </ExerciseStepsStep>
      </div>
    </div>
  </template>
}
