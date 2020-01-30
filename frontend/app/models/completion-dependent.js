import DS from 'ember-data';
const { Model } = DS;
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import arrayPreviousItems from 'brn/utils/array-previous-items';

export default Model.extend({
  tasksManager: service(),
  canInteract: computed(
    'previousSiblings.[]',
    'parent.children.@each.isCompleted',
    'tasksManager.completedTasks.[]',
    function() {
      return (
        !this.previousSiblings.length ||
        this.previousSiblings.every((sibling) => sibling.isCompleted)
      );
    },
  ),
  sortChildrenBy: 'order',
  sortedChildren: computed('children.{[],@each.order}', function() {
    return this.children ? this.children.sortBy(this.sortChildrenBy) : null;
  }),
  isCompleted: computed(
    'tasksManager.completedTasks.[]',
    'children.@each.isCompleted',
    function() {
      return (
        this.get('children').length &&
        this.get('children').every((child) => child.isCompleted)
      );
    },
  ),
  isFirst: computed(function() {
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
