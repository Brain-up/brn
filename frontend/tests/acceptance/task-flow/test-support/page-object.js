import {
  create,
  visitable,
  attribute,
  clickable,
  collection,
  isPresent,
} from 'ember-cli-page-object';

const page = create({
  goToFirstTask: visitable('/groups/1/series/1/exercise/1/task/1'),
  goToFirstTaskSecondExercise: visitable(
    '/groups/1/series/1/exercise/2/task/3',
  ),
  currentTaskId: attribute('data-test-task-id', '[data-test-task-player]'),
  startTask: clickable('[data-test-start-task-button]'),
  chooseRightAnswer: clickable('[data-test-right-answer]'),
  wrongAnswers: collection('[data-test-wrong-answer]', {
    choose: clickable(),
  }),
  startButtonIsPresent: isPresent('[data-test-start-task-button]'),
  correctnessWidgetIndicatesCorrect: attribute(
    'data-test-is-correct',
    '[data-test-answer-correctness-widget]',
  ),
  correctnessWidgetIndicatesIncorrect: attribute(
    'data-test-isnt-correct',
    '[data-test-answer-correctness-widget]',
  ),
  rightAnswerNotificationExists: isPresent(
    '[data-test-right-answer-notification]',
  ),
  secondTaskIsShown: isPresent('[data-test-task-id="2"]'),
});

export default page;
