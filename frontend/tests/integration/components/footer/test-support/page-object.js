import { create, count, text } from 'ember-cli-page-object';

const page = create({
  logosCount: count('[data-test-support-logo]'),
  supportMessageText: text('[data-test-support-message]'),
});

export default page;
