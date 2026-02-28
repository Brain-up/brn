import Model from '@warp-drive/legacy/model';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import TasksManagerService from '../services/tasks-manager';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { cached } from 'tracked-toolbox';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
export default class CompletionDependentModel extends Model {
  declare children: unknown[];
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
    if (!this.children) return null;
    const key = this.sortChildrenBy;
    return Array.from(this.children).filter(Boolean).sort((a: any, b: any) => {
      const aVal = a[key];
      const bVal = b[key];
      if (aVal < bVal) return -1;
      if (aVal > bVal) return 1;
      return 0;
    });
  }

  @cached
  get isCompleted(): boolean {
    if (this.isManuallyCompleted) {
      return true;
    }
    if (this.tasksManager.completedTasks.length === 0) {
      return false;
    }

    const children = this.children;
    const validChildren = children ? Array.from(children).filter(Boolean) : [];
    return validChildren.length > 0 && validChildren.every((child: any) => child.isCompleted)
      ? true
      : false;
  }
  get isFirst() {
    return !this.previousSiblings.length;
  }
  get allSiblings() {
    // @ts-expect-error unknown parent type
    return this.parent?.sortedChildren || [];
  }
  get previousSiblings() {
    return arrayPreviousItems(this, this.allSiblings);
  }
  get nextSiblings() {
    return this.allSiblings.slice(this.allSiblings.indexOf(this) + 1);
  }
}

