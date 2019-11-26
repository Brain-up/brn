import DS from 'ember-data';
const { Model } = DS;
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';

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
  sortedChildren: computed('children.[]', function() {
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
  previousSiblings: computed('parent.sortedChildren.[]', function() {
    const allSiblings = this.parent.get('sortedChildren') || [];
    return allSiblings.slice(0, allSiblings.indexOf(this));
  }),
});
