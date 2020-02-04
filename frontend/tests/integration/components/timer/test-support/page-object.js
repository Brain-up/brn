import { create, text, attribute } from 'ember-cli-page-object';

const page = create({
  timerDisplayValue: text('[data-test-timer-display-value]'),
  timerIsPausedAttr: attribute(
    'data-test-timer-is-paused',
    '[data-test-timer-wrapper]',
  ),
});
export default page;
