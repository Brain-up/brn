import './index.css';
import Component from '@glimmer/component'
import { service } from '@ember/service';
import Router from '@ember/routing/router-service';
import type Store from 'brn/services/store';
import { getOwner } from '@ember/application';
import { LinkTo } from '@ember/routing';
import UiIconHeadphones from 'brn/components/ui/icon/headphones';
import { trackedRef } from 'ember-ref-bucket';
import { action } from '@ember/object';
import { debounce, cancel, type Timer } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { on } from '@ember/modifier';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { fn } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import createRef from 'ember-ref-bucket/modifiers/create-ref';

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
  @trackedRef('breadcrumbs-list') listEl!: HTMLUListElement;
  @tracked scrollIteration = 0;
  debounceTimer: Timer | undefined = undefined;

  modelFor(routeName: string) {
    const owner = getOwner(this);
    if (!owner) return undefined;
    const appRoute = owner.lookup(`route:application`) as { modelFor(name: string): unknown } | undefined;
    if (!appRoute) return undefined;
    const model = appRoute.modelFor(routeName);
    if (routeName === 'group' && model && typeof model === 'object' && 'group' in model && 'series' in model) {
      return (model as Record<string, unknown>).group;
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

  get hasScrollAtAll() {
    this.scrollIteration;
    if (!this.listEl) return false;
    return this.listEl.scrollWidth > this.listEl.offsetWidth;
  }

  get showLeftScrollButton() {
    return this.hasScrollAtAll && this.listEl?.scrollLeft > 0;
  }

  get showRightScrollButton() {
    if (!this.hasScrollAtAll) return false;
    const scrollSize = this.listEl.offsetWidth + this.listEl.scrollLeft;
    return scrollSize < this.listEl.scrollWidth;
  }

  @action scroll(direction: 'right' | 'left') {
    const position = this.listEl.scrollLeft;
    const offset = 150;
    const newPosition =
      direction === 'right' ? position + offset : position - offset;
    this.listEl.scrollTo({ top: 0, left: newPosition, behavior: 'smooth' });
  }

  @action onScroll() {
    cancel(this.debounceTimer);
    this.debounceTimer = debounce(this, this.updateScroll, 100);
  }

  updateScroll() {
    this.scrollIteration++;
  }

  willDestroy() {
    super.willDestroy();
    cancel(this.debounceTimer);
  }

  <template>
    <div
      class="breadcrumbs-container"
      ...attributes
    >
      {{#if this.showLeftScrollButton}}
        <div class="scroll-fade scroll-fade--left"></div>
        <button
          type="button"
          class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-none absolute left-0 z-20 flex items-center justify-center w-6 h-6 text-white rounded-full shadow-md"
          {{on "click" (fn this.scroll "left")}}
          aria-label="Scroll left"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
          </svg>
        </button>
      {{/if}}

      <ul
        aria-label="Breadcrumbs"
        class="breadcrumbs list-none"
        {{createRef "breadcrumbs-list"}}
        {{on "scroll" this.onScroll}}
      >
        <li class="flex-shrink-0">
          <LinkTo @route="groups" class="inline-block align-top">
            <UiIconHeadphones />
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

      {{#if this.showRightScrollButton}}
        <div class="scroll-fade scroll-fade--right"></div>
        <button
          type="button"
          class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-none absolute right-0 z-20 flex items-center justify-center w-6 h-6 text-white rounded-full shadow-md"
          {{on "click" (fn this.scroll "right")}}
          aria-label="Scroll right"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
          </svg>
        </button>
      {{/if}}
    </div>
  </template>
}
