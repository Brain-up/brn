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
import { eq } from 'ember-truth-helpers';

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

  // Base Tailwind classes for all step buttons
  static BASE_BTN = 'exercise-step-btn border-2 border-solid cursor-pointer';
  // State-specific Tailwind classes (colors that must override Tailwind preflight)
  static STATE_DEFAULT = 'border-[#e5e7eb] bg-white text-[#6b7280]';
  static STATE_ACTIVE = 'exercise-step-btn--active border-[#428dfc] bg-[#eef4ff] text-[#2d4adb] shadow-[0_0_0_3px_rgba(66,141,252,0.15),0_2px_8px_rgba(66,141,252,0.2)]';
  static STATE_COMPLETED = 'exercise-step-btn--completed border-[#86efac] bg-[#f0fdf4] text-[#16a34a]';
  static STATE_NEXT = 'exercise-step-btn--next border-[#93c5fd] bg-[#f0f7ff] text-[#3b82f6]';
  static STATE_LOCKED = 'exercise-step-btn--locked border-[#f3f4f6] bg-[#fafafa] text-[#d1d5db] cursor-not-allowed opacity-70';

  get listenBtnClass() {
    const base = ExerciseStepsComponent.BASE_BTN;
    if (this.modeForListen === BUTTONS.ACTIVE) return `${base} ${ExerciseStepsComponent.STATE_ACTIVE}`;
    if (this.isListenCompleted) return `${base} ${ExerciseStepsComponent.STATE_COMPLETED}`;
    return `${base} ${ExerciseStepsComponent.STATE_DEFAULT}`;
  }
  get interactBtnClass() {
    const base = ExerciseStepsComponent.BASE_BTN;
    if (this.modeForInteract === BUTTONS.ACTIVE) return `${base} ${ExerciseStepsComponent.STATE_ACTIVE}`;
    if (this.isInteractCompleted) return `${base} ${ExerciseStepsComponent.STATE_COMPLETED}`;
    if (this.isListenCompleted) return `${base} ${ExerciseStepsComponent.STATE_NEXT}`;
    return `${base} ${ExerciseStepsComponent.STATE_DEFAULT}`;
  }
  get taskBtnClass() {
    const base = ExerciseStepsComponent.BASE_BTN;
    if (this.modeForTask === BUTTONS.ACTIVE) return `${base} ${ExerciseStepsComponent.STATE_ACTIVE}`;
    if (this.modeForTask === BUTTONS.DISABLED) return `${base} ${ExerciseStepsComponent.STATE_LOCKED}`;
    if (this.isInteractCompleted) return `${base} ${ExerciseStepsComponent.STATE_NEXT}`;
    return `${base} ${ExerciseStepsComponent.STATE_DEFAULT}`;
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
      <button
        type="button"
        class={{this.listenBtnClass}}
        disabled={{false}}
        {{on "click" (fn this.onClick this.MODES.LISTEN this.modeForListen)}}
      >
        <span class="exercise-step-btn__badge">
          {{#if this.isListenCompleted}}
            <svg class="exercise-step-btn__check" viewBox="0 0 16 16" fill="none"><path d="M3 8.5L6.5 12L13 4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{else}}
            1
          {{/if}}
        </span>
        <span class="exercise-step-btn__label">{{t "control_exercises.listen"}}</span>
      </button>

      {{!-- Connector 1 --}}
      <div class={{this.connector1Class}}><div class="exercise-connector__line"></div></div>

      {{!-- Step 2: Interact --}}
      <button
        type="button"
        class={{this.interactBtnClass}}
        disabled={{false}}
        {{on "click" (fn this.onClick this.MODES.INTERACT this.modeForInteract)}}
      >
        <span class="exercise-step-btn__badge">
          {{#if this.isInteractCompleted}}
            <svg class="exercise-step-btn__check" viewBox="0 0 16 16" fill="none"><path d="M3 8.5L6.5 12L13 4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{else}}
            2
          {{/if}}
        </span>
        <span class="exercise-step-btn__label">{{t "control_exercises.interact"}}</span>
      </button>

      {{!-- Connector 2 --}}
      <div class={{this.connector2Class}}><div class="exercise-connector__line"></div></div>

      {{!-- Step 3: Solve --}}
      <button
        type="button"
        class={{this.taskBtnClass}}
        disabled={{eq this.modeForTask "disabled"}}
        {{on "click" (fn this.onClick this.MODES.TASK this.modeForTask)}}
      >
        <span class="exercise-step-btn__badge">3</span>
        <span class="exercise-step-btn__label">{{t "control_exercises.solve"}}</span>
      </button>
    </div>
  </template>
}
