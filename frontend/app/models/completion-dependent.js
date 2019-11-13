import DS from 'ember-data';
const { Model } = DS;
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';

export default Model.extend({
  tasksManager: service(),
  canInteract: computed(
    'tasksManager.completedTasks.[]',
    'parent.children.@each.isCompleted',
    function() {
      const parent = this.parent.content || this.parent;
      return parent.canInteractWith.apply(this.parent, [this]);
    },
  ),
  isCompleted: computed(
    'tasksManager.completedTasks.[]',
    'children.@each.isCompleted',
    function() {
      return this.get('children').every((child) => child.isCompleted);
    },
  ),
  canInteractWith(child) {
    const children = this.get('children');
    const previousChildren = children.slice(0, children.indexOf(child));
    return (
      !previousChildren.length ||
      previousChildren.every((previousChild) => previousChild.isCompleted)
    );
  },
});
