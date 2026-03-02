import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array, get } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import arrayLast from 'brn/helpers/array-last';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import arrayFirst from 'brn/helpers/array-first';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ExerciseType from 'brn/components/exercise-type/template';

export default RouteTemplate(
  <template>
    {{pageTitle (t "groups.title")}}
    <div class="sm:text-base sm:mt-6 flex justify-center mt-0 text-sm"><h3>{{t
          "groups.exercise_selection"
        }}</h3></div>
    <ol
      class="series-container sm:flex-row flex flex-col items-center justify-center mt-3"
    >
      <li class="list-item sm:mr-6 mr-0">
        <LinkTo @route="group" @model={{arrayLast @model}} title={{get (arrayLast @model) "description"}}>
          <ExerciseType
            @initial={{get (arrayLast @model) "name"}}
            @img="speech-exercises"
          />
        </LinkTo>
      </li>
      <li class="list-item sm:mt-0 mt-3">
        <LinkTo @route="group" @model={{arrayFirst @model}} title={{get (arrayFirst @model) "description"}}>
          <ExerciseType
            @initial={{get (arrayFirst @model) "name"}}
            @img="non-speech-exercises"
          />
        </LinkTo>
      </li>
    </ol>
  </template>
);
