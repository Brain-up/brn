import Helper from '@ember/component/helper';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';

export default class IsActive extends Helper {
  @service declare router: RouterService;

  compute([routeName]: [string]): boolean {
    return this.router.isActive(routeName);
  }
}
