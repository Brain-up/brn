import Helper from '@ember/component/helper';
import { getOwner } from '@ember/application';

export default class ModelForRoute extends Helper {
  compute([routeName]) {
    return getOwner(this).lookup(`route:application`).modelFor(routeName);
  }
}
