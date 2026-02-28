import Component from '@glimmer/component'
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';
import type Store from 'brn/services/store';
import { getOwner } from '@ember/application';

interface InternalRouter {
  _router: {
    currentState?: {
      router: {
        state: {
          params: Record<string, Record<string, string>>;
        };
      };
    };
  };
}

export default class BreadcrumbsComponent extends Component {
  @service('router') router!: Router;
  @service('store') store!: Store;
  modelFor(routeName: string) {
    return  getOwner(this).lookup(`route:application`).modelFor(routeName);
  }
  get parts() {
    this.router.currentURL;
    const internalRouter = this.router as unknown as InternalRouter;
    const params = internalRouter._router.currentState?.router.state.params || {};
    const parts = Object.keys(params).sort((a,b) => {
        return a.split('.').length - b.split('.').length;
    }).filter(key => {
        return Object.keys(params[key]).length;
    }).map(key => {
        const ref = Object.keys(params[key])[0];
        const modelName = ref.split('_')[0];
        const modelId = params[key][ref];
        const record = this.store.peekRecord(modelName, modelId) as { name?: string } | null;
        return {
          route: key,
          model: this.modelFor(key),
          name: record?.name
        };
    })

    return parts;
  }
}
