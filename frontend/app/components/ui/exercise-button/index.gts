import Component from '@glimmer/component';
import type { Exercise } from 'brn/schemas/exercise';
import { LinkTo } from '@ember/routing';
import { not } from 'ember-truth-helpers';
import { concat } from '@ember/helper';
import { t } from 'ember-intl';
import UiIconCheck from 'brn/components/ui/icon/check';

interface UiExerciseButtonSignature {
  Args: {
    title?: string | number;
    exercise: Exercise;
    isAvailable: boolean;
  };
  Element: HTMLAnchorElement;
}

export default class UiExerciseButtonComponent extends Component<UiExerciseButtonSignature> {
  get classes() {
    const items = ['focus:outline-none'];
    if (this.mode) {
      items.push(this.mode);
    }
    return items.join(' ');
  }
  get mode() {
    if (this.isDisabled) {
      return 'disabled';
    }
    if (this.isCompleted) {
      return 'completed';
    }
    if (this.isLocked) {
      return 'locked';
    }
    return 'active';
  }
  get isDisabled() {
    if (this.args.isAvailable === false) {
      return true;
    } else {
      return false;
    }
  }

  get isCompleted() {
    return this.args.exercise.isCompleted;
  }

  get isActive() {
    return this.mode === 'active';
  }

  get isLocked() {
    return !this.args.isAvailable;
  }

  get titleClasses() {
    if (this.mode === 'locked') {
      return 'title title-locked';
    }

    if (this.mode === 'disabled') {
      return 'title title-disabled';
    }
    return 'title';
  }

  <template>
    <LinkTo
      class="{{this.classes}}"
      aria-disabled={{if (not @isAvailable) "true"}}
      @route="group.series.subgroup.exercise"
      @model={{@exercise.id}}
      title={{concat (t "task_link.exercise") " " @exercise.level}}
      ...attributes
    >
      <div class={{this.titleClasses}}>
        {{@title}}
      </div>
      <div class="check-container">
        <UiIconCheck
          @isCompleted={{this.isCompleted}}
          @isLocked={{this.isLocked}}
          @isDisabled={{this.isDisabled}}
          @isActive={{this.isActive}}
        />
      </div>
    </LinkTo>
  </template>
}
