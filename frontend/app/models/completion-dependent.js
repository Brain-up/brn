import Model from '@ember-data/model';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import arrayPreviousItems from 'brn/utils/array-previous-items';

export default Model.extend({
  tasksManager: service(),
  canInteract: computed(
    'available',
    'previousSiblings.[]',
    'parent.children.@each.isCompleted',
    'tasksManager.completedTasks.[]',
    function() {
      if (this.available) {
        return true;
      }
      return (
        !this.previousSiblings.length ||
        this.previousSiblings.every((sibling) => sibling.isCompleted)
      );
    },
  ),
  sortChildrenBy: 'order',
  // eslint-disable-next-line ember/require-computed-property-dependencies
  sortedChildren: computed('children.{[],@each.order}', function() {
    return this.children ? this.children.sortBy(this.sortChildrenBy) : null;
  }),
  isManuallyCompleted: false,
  isCompleted: computed(
    'isManuallyCompleted',
    'tasksManager.completedTasks.[]',
    'children.@each.isCompleted',
    function() {
      if (this.isManuallyCompleted) {
        return true;
      }
      if (this.tasksManager.completedTasks.length === 0) {
        return false;
      }
      // eslint-disable-next-line ember/no-get
      const children = this.get('children');
      return (
        children.length &&
        children.every((child) => child.isCompleted)
      );
    },
  ),
  // eslint-disable-next-line ember/require-computed-macros
  isFirst: computed('previousSiblings.length', function() {
    return !this.previousSiblings.length;
  }),
  allSiblings: computed('parent.sortedChildren.[]', function() {
    return this.parent.get('sortedChildren') || [];
  }),
  previousSiblings: computed('allSiblings.[]', function() {
    return arrayPreviousItems(this, this.allSiblings);
  }),
  nextSiblings: computed('allSiblings.[]', function() {
    return this.allSiblings.slice(this.allSiblings.indexOf(this) + 1);
  }),
});
