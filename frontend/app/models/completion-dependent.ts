import Model from '@ember-data/model';
import { inject as service } from '@ember/service';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import TasksManagerService from '../services/tasks-manager';
import { cached } from 'tracked-toolbox';
import { tracked } from '@glimmer/tracking';
export default class CompletionDependentModel extends Model {
  available!: boolean;
  // public parent!: any;
  // public children!: CompletionDependentModel[];
  sortChildrenBy = 'order';
  @tracked
  isManuallyCompleted = false;

  @service('tasks-manager') tasksManager!: TasksManagerService;
  get canInteract() {
    if (this.available) {
      return true;
    }
    return (
      !this.previousSiblings.length ||
      this.previousSiblings.every(
        (sibling: CompletionDependentModel) => sibling.isCompleted,
      )
    );
  }

  get sortedChildren() {
    return this.children ? this.children.sortBy(this.sortChildrenBy) : null;
  }

  @cached
  get isCompleted(): boolean {
    if (this.isManuallyCompleted) {
      return true;
    }
    if (this.tasksManager.completedTasks.length === 0) {
      return false;
    }
    // eslint-disable-next-line ember/no-get
    const children = this.get('children');
    return children.length && children.every((child) => child.isCompleted)
      ? true
      : false;
  }
  get isFirst() {
    return !this.previousSiblings.length;
  }
  get allSiblings() {
    return this.parent.get('sortedChildren') || [];
  }
  get previousSiblings() {
    return arrayPreviousItems(this, this.allSiblings);
  }
  get nextSiblings() {
    return this.allSiblings.slice(this.allSiblings.indexOf(this) + 1);
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'completion-dependent': CompletionDependentModel;
  }
}
