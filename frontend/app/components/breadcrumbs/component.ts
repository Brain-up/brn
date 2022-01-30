import Component from '@glimmer/component'
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';
import Store from '@ember-data/store';
import { getOwner } from '@ember/application';

export default class BreadcrumbsComponent extends Component {
  @service('router') router!: Router;
  @service('store') store!: Store;
  modelFor(routeName: string) {
    return  getOwner(this).lookup(`route:application`).modelFor(routeName);
  }
  get parts() {
    this.router.currentURL;
    const params = this.router._router.currentState?.router.state.params || {};
    const parts = Object.keys(params).sort((a,b) => {
        return a.split('.').length - b.split('.').length;
    }).filter(key => {
        return Object.keys(params[key]).length;
    }).map(key => {
        const ref = Object.keys(params[key])[0];
        const modelName = ref.split('_')[0];
        const modelId = params[key][ref];
        return {
          route: key,
          model: this.modelFor(key),
          name: this.store.peekRecord(modelName, modelId)?.name
        };
    })

    return parts;
  }
}
