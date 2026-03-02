import Component from '@glimmer/component';
import type { Exercise } from 'brn/schemas/exercise';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { or } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import contains from 'brn/helpers/contains';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiExerciseButton from 'brn/components/ui/exercise-button';

interface SeriesNavigationSignature {
  Args: {
    exercises: Exercise[];
    available?: (string | number)[];
  };
  Element: HTMLElement;
}

interface ExerciseGroup {
  name: string;
  exercises: Exercise[];
}

export default class SeriesNavigationComponent extends Component<SeriesNavigationSignature> {
  get sortedExercises(): Exercise[] {
    return Array.from(this.args.exercises).sort((a, b) => a.level - b.level);
  }

  get exerciseGroups(): ExerciseGroup[] {
    const headers = [...new Set(this.sortedExercises.map((e) => e.name))];
    return headers.map((name) => ({
      name,
      exercises: this.sortedExercises
        .filter((e) => e.name === name)
        .sort((a, b) => a.level - b.level),
    }));
  }

  <template>
    <div ...attributes>
      {{#each this.exerciseGroups as |group|}}
        <div class="mx-auto mb-10">
          <div class="max-w-none mx-auto">
            <div class="sm:rounded-lg pb-4 overflow-hidden">
              <div class="sm:px-6 hidden px-4 py-5">
                {{!-- remove hidden when "Noise filter will be implemented" --}}
                <h3
                  data-test-series-navigation-header
                  class="navigation-block__header text-lg font-bold leading-3"
                >
                  {{group.name}}
                </h3>
              </div>
              <div
                data-test-exercises-name-group
              >
                <h3 class="sm:pl-4 pl-2">{{group.name}}</h3>
                <div class="sm:grid-cols-4 md:grid-cols-5 sm:gap-3 grid justify-center grid-cols-3 gap-2 mx-2">
                  {{#each group.exercises as |exercise|}}
                    {{#let
                      (contains exercise.id (or @available (array)))
                      as |isAvailable|
                    }}
                      <div class="flex justify-center">
                        <UiExerciseButton
                          @exercise={{exercise}}
                          @title={{exercise.level}}
                          @isAvailable={{isAvailable}}
                          class="exercise-block__level flex"
                          data-test-series-navigation-list-link
                          data-test-exercise-level={{exercise.level}}
                          data-test-exercise-name={{exercise.name}}
                        />
                      </div>
                    {{/let}}
                  {{/each}}
                </div>
              </div>
            </div>
          </div>
        </div>
      {{/each}}
    </div>
  </template>
}
