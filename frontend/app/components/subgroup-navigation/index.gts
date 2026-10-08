import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiTabButton from 'brn/components/ui/tab-button';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiHorizontalScroll from 'brn/components/ui/horizontal-scroll';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import autofitText from 'brn/modifiers/autofit-text';

interface SubgroupItem {
  id?: string | number | null;
  name?: string;
  description?: string;
  order?: number;
}

interface SubgroupNavigationSignature {
  Args: {
    group: unknown;
  };
  Element: HTMLDivElement;
}

export default class SubgroupNavigation extends Component<SubgroupNavigationSignature> {
  get sortedExercises(): SubgroupItem[] {
    const group = this.args.group as SubgroupItem[] | undefined;
    if (!group || !Array.isArray(group)) return [];
    return [...group].sort((a, b) => {
      const aOrder = a.order ?? 0;
      const bOrder = b.order ?? 0;
      return aOrder - bOrder;
    });
  }

  <template>
    <UiHorizontalScroll ...attributes>
      {{#each this.sortedExercises as |exercise|}}
        <li class="item">
          <UiTabButton
            data-test-active-link={{exercise.name}}
            class="pl-3 pr-3"
            @small={{true}}
            @route="group.series.subgroup"
            @models={{array exercise.id}}
            @title={{exercise.name}}
            @tooltip={{exercise.description}}
            {{autofitText exercise.name}}
          />
        </li>
      {{/each}}
    </UiHorizontalScroll>
  </template>
}
