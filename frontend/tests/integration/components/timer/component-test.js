import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, clearRender } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | timer', function(hooks) {
  setupRenderingTest(hooks);

  test('starts timer when added', async function(assert) {
    this.set('runTimer', function() {
      assert.ok(true, 'started timer');
    });
    await render(hbs`<Timer @runTimer={{this.runTimer}}/>`);

    clearRender();
  });

  test('has mm:ss format', async function(assert) {
    this.set('countedSeconds', 67);
    this.set('runTimer', function() {});
    await render(
      hbs`<Timer @countedSeconds={{this.countedSeconds}} @runTimer={{this.runTimer}}/>`,
    );

    assert.dom('[data-test-timer-display-value]').hasText('01:07');
  });

  test('continues with time from studying-timer', async function(assert) {
    this.set('studyingTimer', {
      countedSeconds: 94,
    });
    await render(
      hbs`<Timer @studyingTimer={{this.studyingTimer}} @isPaused={{true}}/>`,
    );

    assert.dom('[data-test-timer-display-value]').hasText('01:34');
  });
});
