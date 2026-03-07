import Component from '@glimmer/component';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ExerciseType from 'brn/components/exercise-type';
import type { Group } from 'brn/schemas/group';

interface Signature {
  Args: {
    model: Group[];
  };
}

class GroupsTemplate extends Component<Signature> {
  get lastGroup(): Group {
    const model = this.args.model;
    return model[model.length - 1]!;
  }

  get firstGroup(): Group {
    const model = this.args.model;
    return model[0]!;
  }

  <template>
    {{pageTitle (t "groups.title")}}
    <div class="sm:text-base sm:mt-6 flex justify-center mt-0 text-sm"><h3>{{t
          "groups.exercise_selection"
        }}</h3></div>
    <ol
      class="series-container sm:flex-row flex flex-col items-center justify-center mt-3"
    >
      <li class="list-item sm:mr-6 mr-0">
        <LinkTo @route="group" @model={{this.lastGroup}} title={{this.lastGroup.description}}>
          <ExerciseType
            @initial={{this.lastGroup.name}}
            @img="speech-exercises"
          />
        </LinkTo>
      </li>
      <li class="list-item sm:mt-0 mt-3">
        <LinkTo @route="group" @model={{this.firstGroup}} title={{this.firstGroup.description}}>
          <ExerciseType
            @initial={{this.firstGroup.name}}
            @img="non-speech-exercises"
          />
        </LinkTo>
      </li>
    </ol>
  </template>
}

export default RouteTemplate(GroupsTemplate);
