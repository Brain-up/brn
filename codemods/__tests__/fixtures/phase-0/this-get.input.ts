/* eslint-disable ember/no-get */
import Model from '@ember-data/model';

export default class Exercise extends Model {
  get previousSiblings() {
    return this.get('series.groupedByNameExercises')[this.name];
  }

  get sortedChildren() {
    const children = this.get('children');
    return children;
  }

  get simpleProp() {
    return this.get('name');
  }
}
