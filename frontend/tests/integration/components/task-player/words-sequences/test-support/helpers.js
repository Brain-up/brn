import { click } from '@ember/test-helpers';

export async function chooseAnswer(option) {
  await click(`[data-test-task-answer-option="${option}"]`);
}
