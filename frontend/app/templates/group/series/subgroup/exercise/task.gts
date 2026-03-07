import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import queue from 'brn/helpers/queue';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import TaskPlayer from 'brn/components/task-player';
import type GroupSeriesSubgroupExerciseTaskController from 'brn/controllers/group/series/subgroup/exercise/task';
import type { TaskBase } from 'brn/schemas/task';

interface Signature {
  Args: {
    model: TaskBase;
    controller: GroupSeriesSubgroupExerciseTaskController;
  };
}

const tpl: TOC<Signature> = <template>
    <TaskPlayer
      data-test-task-player
      data-test-task-id={{@model.id}}
      data-test-task-exercise-id={{@model.exercise.id}}
      @onRightAnswer={{queue @controller.nextTaskTransition @model.savePassed}}
      @task={{@model}}
    />
  </template>;

export default RouteTemplate(tpl);
