import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiTabButton from 'brn/components/ui/tab-button';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiHorizontalScroll from 'brn/components/ui/horizontal-scroll';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import autofitText from 'brn/modifiers/autofit-text';

interface SeriesItem {
  id?: string | null;
  name?: string;
  description?: string;
}

interface GroupNavigationSignature {
  Args: {
    series?: unknown[];
    group?: unknown;
  };
  Element: HTMLDivElement;
}

export default class GroupNavigationComponent extends Component<GroupNavigationSignature> {
  get sortedSeries(): SeriesItem[] {
    const group = this.args.group as { series?: SeriesItem[] } | undefined;
    const series = (this.args.series ?? group?.series ?? []) as SeriesItem[];
    return Array.from(series).sort((a, b) => {
      const aId = String(a.id ?? '');
      const bId = String(b.id ?? '');
      return aId.localeCompare(bId);
    });
  }

  <template>
    <UiHorizontalScroll ...attributes>
      {{#each this.sortedSeries as |series|}}
        <li class="item">
          <UiTabButton
            data-test-active-link={{series.name}}
            @route="group.series"
            @models={{array series.id}}
            @title={{series.name}}
            @tooltip={{series.description}}
            {{autofitText series.name}}
          />
        </li>
      {{/each}}
    </UiHorizontalScroll>
  </template>
}
