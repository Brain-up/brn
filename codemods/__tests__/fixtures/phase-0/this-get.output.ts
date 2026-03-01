/* eslint-disable ember/no-get */
import Model from '@ember-data/model';

export default class Exercise extends Model {
  get previousSiblings() {
    return this.series?.groupedByNameExercises[this.name];
  }

  get sortedChildren() {
    const children = this.children;
    return children;
  }

  get simpleProp() {
    return this.name;
  }
}
