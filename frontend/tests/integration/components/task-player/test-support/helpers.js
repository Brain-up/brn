import { click } from '@ember/test-helpers';
import deepEqual from 'brn/utils/deep-equal';

export async function chooseAnswer(option) {
  await click(`[data-test-task-answer-option=${option}]`);
}

export function optionsHaveBeenShaffled(previous, current) {
  return (
    previous.every((option) => current.includes(option)) &&
    !deepEqual(previous, current)
  );
}

export async function goToNextTask() {
  await click('[data-test-next-task-button]');
}
