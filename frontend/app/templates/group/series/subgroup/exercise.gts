import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { eq } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { and, not } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import willDestroy from '@ember/render-modifiers/modifiers/will-destroy';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ModalDialog from 'ember-modal-dialog/components/modal-dialog';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import AnswerCorrectnessWidget from 'brn/components/answer-correctness-widget/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ExerciseStats from 'brn/components/exercise-stats/component';

export default RouteTemplate(
  <template>
    <ModalDialog
      @overlayClass="z-50 min-h-full w-full fixed bg-blue-400 flex"
      @containerClass="w-full flex bg-white sm:p-2 p-2 rounded-lg text-2xl justify-center"
    >
      <div
        class="series-page--canvas flex flex-col flex-grow max-w-screen-xl"
        {{didInsert @controller.disableBodyScroll}}
        {{willDestroy @controller.enableBodyScroll}}
        {{didInsert @controller.startStatsTracking @model}}
        {{willDestroy @controller.stopStatsTracking @model}}
      >
        <div class="fixed" id="modal-close-button">
          <LinkTo @route="group.series.subgroup" title={{t "navigation.come_back"}}>
            <svg
              width="32"
              height="32"
              viewBox="0 0 32 32"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M23 23L9 9"
                stroke="black"
                stroke-linecap="round"
                stroke-linejoin="round"
              ></path>
              <path
                d="M23 9L9 23"
                stroke="black"
                stroke-linecap="round"
                stroke-linejoin="round"
              ></path>
            </svg>
          </LinkTo>
        </div>
        {{#if
          (eq @controller.target.currentRouteName "group.series.subgroup.exercise.task")
        }}
          <div class="flex items-center justify-between">
            <div class="sm:w-32 flex-shrink-0 w-10"></div>
            <div class="sm:mr-4 flex-1 min-w-0 mr-2" id="progress-slot"></div>
            <div class="flex-shrink-0" id="exercise-config-slot"></div>
            <div class="flex-shrink-0" id="exercise-timer-slot"></div>
          </div>
        {{/if}}
        <div class="exercise-container">
          {{#if
            (and
              @controller.exerciseIsCompletedInCurrentCycle (not @controller.showExerciseStats)
            )
          }}
            <AnswerCorrectnessWidget
              @isCorrect={{true}}
              {{didInsert @controller.greedOnCompletedExercise}}
            />
          {{else if @controller.showExerciseStats}}
            <ExerciseStats
              @stats={{@controller.modelStats}}
              @onComplete={{@controller.afterCompleted}}
            />
          {{else}}
            {{outlet}}
          {{/if}}
        </div>
      </div>
    </ModalDialog>
  </template>
);
