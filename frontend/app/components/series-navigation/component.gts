import Component from '@glimmer/component';
import type { Exercise } from 'brn/schemas/exercise';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import sortBy from 'brn/helpers/sort-by';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import filterBy from 'brn/helpers/filter-by';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import contains from 'brn/helpers/contains';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { or } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { get } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiExerciseButton from 'brn/components/ui/exercise-button';

interface SeriesNavigationSignature {
  Args: {
  exercises: Exercise[];
  };
  Element: HTMLElement;
}

export default class SeriesNavigationComponent extends Component<SeriesNavigationSignature> {
  get sortedExercises() {
    return Array.from(this.args.exercises).sort((a, b) => a.level - b.level);
  }

  get exerciseHeaders() {
    return [...new Set(this.sortedExercises.map((e) => e.name))];
  }

  <template>
    <div ...attributes>
      {{#each this.exerciseHeaders as |header|}}
        <div class="mx-auto mb-10">
          <div class="max-w-none mx-auto">
            <div class="sm:rounded-lg pb-4 overflow-hidden">
              <div class="sm:px-6 hidden px-4 py-5">
                //remove hidden when "Noise filter will be implemented"
                <h3
                  data-test-series-navigation-header
                  class="navigation-block__header text-lg font-bold leading-3"
                >
                  {{header}}
                </h3>
              </div>
              {{#let
                (sortBy "level" (filterBy "name" header this.sortedExercises))
                as |slices|
              }}
                <div
                  data-test-exercises-name-group
                >
                  <h3 class="pl-4">{{get (get slices '0') 'name'}}</h3>
                  <div class="sm:grid-cols-4 md:grid-cols-5 gap-y-2 sm:gap-y-3 grid justify-center grid-cols-3 mx-2 ml-0">
                    {{#each slices as |exercise|}}
                    {{#let
                      (contains exercise.id (or @available (array)))
                      as |isAvailable|
                    }}
                      <div class="flex">
                        <UiExerciseButton
                          @exercise={{exercise}}
                          @title={{exercise.level}}
                          @isAvailable={{isAvailable}}
                          class="exercise-block__level flex mt-2 ml-2 mr-2"
                          data-test-series-navigation-list-link
                          data-test-exercise-level={{exercise.level}}
                          data-test-exercise-name={{exercise.name}}
                        />
                      </div>
                    {{/let}}
                  {{/each}}
                
                  </div>
                  </div>
              {{/let}}
            </div>
          </div>
        </div>
      {{/each}}
    </div>
  </template>
}
