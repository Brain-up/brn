import { create, clickable, attribute } from 'ember-cli-page-object';

const page = create({
  playAudio: clickable('[data-test-play-audio-button]'),
  buttonIsDisabled: attribute('disabled', '[data-test-play-audio-button]'),
  progressValue: attribute(
    'data-test-playing-progress',
    '[data-test-play-audio-button]',
  ),
});

export default page;
