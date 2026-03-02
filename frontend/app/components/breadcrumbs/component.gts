import Component from '@glimmer/component'
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
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
    const model = getOwner(this).lookup(`route:application`).modelFor(routeName);
    // Group route returns a composite { group, series } model;
    // extract the group record for LinkTo compatibility.
    // Use 'in' check to avoid triggering WarpDrive's strict proxy on a bare record.
    if (routeName === 'group' && model && 'group' in model && 'series' in model) {
      return model.group;
    }
    return model;
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

  <template>
    <div
      class="breadcrumbs-container"
      ...attributes
    >
    
        <ul aria-label="Breadcrumbs" class="breadcrumbs list-none">
          <li>
            <LinkTo @route="groups" class="inline-block align-top">
              <Ui::Icon::Headphones />
            </LinkTo>
          </li>
          {{#each this.parts as |part|}}
              <li class="font-bold text-blue-900" data-test-breadcrumb={{part.route}}>
                <LinkTo @route={{part.route}} @model={{part.model}}>
                  {{part.name}}
                </LinkTo>
              </li>
          {{/each}}
        </ul>
    
    </div>
  </template>
}
