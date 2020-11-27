import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';
export default class BreadcrumbsComponent extends Component {
  @service('router') router!: Router;
  get groupSubname() {
    return this.router.currentRoute.queryParams?.name || '';
  }
}
