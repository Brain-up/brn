import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <ModalDialog
      @overlayClass="z-50 min-h-full w-full fixed bg-blue-400 flex"
      @containerClass="w-full flex bg-white sm:p-2 p-2 rounded-lg text-2xl justify-center"
    >
      <div
        class="series-page--canvas flex flex-col flex-grow max-w-screen-xl"
        {{did-insert @controller.disableBodyScroll}}
        {{will-destroy @controller.enableBodyScroll}}
        {{did-insert @controller.startStatsTracking @model}}
        {{will-destroy @controller.stopStatsTracking @model}}
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
              {{did-insert @controller.greedOnCompletedExercise}}
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
