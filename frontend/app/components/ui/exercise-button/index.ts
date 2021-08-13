import Component from '@glimmer/component';
import Exercise from 'brn/models/exercise';

interface UiExerciseButtonComponentArguments {
  title?: string;
  exercise: Exercise;
  isAvailable: boolean;
}

export default class UiExerciseButtonComponent extends Component<UiExerciseButtonComponentArguments> {
  get classes() {
    const items = ['focus:outline-none mb-2'];
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
}
