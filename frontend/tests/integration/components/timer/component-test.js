import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import customTimeout from 'brn/utils/custom-timeout';
import pageObject from './test-support/page-object';

module('Integration | Component | timer', function(hooks) {
  setupRenderingTest(hooks);

  test('supports mm:ss format', async function(assert) {
    this.set('countedSeconds', 67);
    this.set('startTimer', function() {});
    await render(
      hbs`<Timer @countedSeconds={{this.countedSeconds}} @startTimer={{this.startTimer}}/>`,
    );

    assert.equal(pageObject.timerDisplayValue, '01:07', 'value is 01:07');
  });

  test('supports hh:mm:ss format', async function(assert) {
    this.set('countedSeconds', 3705);
    this.set('startTimer', function() {});
    await render(
      hbs`<Timer @countedSeconds={{this.countedSeconds}} @startTimer={{this.startTimer}}/>`,
    );

    assert.equal(pageObject.timerDisplayValue, '01:01:45', 'value is 01:01:45');
  });

  test('continues with time from studying-timer', async function(assert) {
    this.set('studyingTimer', {
      countedSeconds: 94,
      pause() {},
      register() {},
      togglePause() {},
    });
    await render(
      hbs`<Timer @studyingTimer={{this.studyingTimer}} @paused={{true}}/>`,
    );

    assert.equal(pageObject.timerDisplayValue, '01:34', 'value is 01:34');
  });

  test('pauses on idle', async function(assert) {
    await render(hbs`<Timer @idleTimeout={{2}}/>`);

    assert.notOk(pageObject.timerIsPausedAttr, 'timer isnt paused');

    await customTimeout();

    assert.equal(pageObject.timerIsPausedAttr, '', 'timer is paused');
  });
});
