import { create, clickable } from 'ember-cli-page-object';

const page = create({
  playAudio: clickable('[data-test-play-audio-button]'),
});

export default page;
