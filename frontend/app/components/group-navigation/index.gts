import sortBy from 'brn/helpers/sort-by';
import { or } from 'ember-truth-helpers';
import { array } from '@ember/helper';
import UiTabButton from 'brn/components/ui/tab-button';
import autofitText from 'brn/modifiers/autofit-text';

<template>
  <div ...attributes>
    <ul class="hs full no-scrollbar">
      {{#each (sortBy "id" (or @series @group.series)) as |series|}}
        <li class="item">
          <UiTabButton
            data-test-active-link={{series.name}}
            class="pl-3 pr-3"
            @route="group.series"
            @models={{array series.id}}
            @title={{series.name}}
            @tooltip={{series.description}}
            {{autofitText series.name}}
          />
        </li>
      {{/each}}
    </ul>
  </div>
</template>
