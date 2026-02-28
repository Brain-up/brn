import Helper from '@ember/component/helper';
import { getOwner } from '@ember/application';

export default class ModelForRoute extends Helper {
  compute([routeName]) {
    const model = getOwner(this).lookup(`route:application`).modelFor(routeName);
    // Group route returns a composite { group, series } model;
    // extract the group record for compatibility.
    if (routeName === 'group' && model?.group) {
      return model.group;
    }
    return model;
  }
}
