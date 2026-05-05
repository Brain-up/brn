import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Service | studying-timer', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let service = this.owner.lookup('service:studying-timer');
    assert.ok(service);
  });

  module('maybeIdlePause', function () {
    test('pauses when audio is not playing', function (assert) {
      this.owner.register(
        'service:audio',
        class extends Service { isPlaying = false; },
      );
      const timer = this.owner.lookup('service:studying-timer');
      timer.maybeIdlePause();
      assert.true(timer.isPaused, 'isPaused set to true');
    });

    test('does not pause when audio is playing', function (assert) {
      this.owner.register(
        'service:audio',
        class extends Service { isPlaying = true; },
      );
      const timer = this.owner.lookup('service:studying-timer');
      timer.maybeIdlePause();
      assert.false(timer.isPaused, 'isPaused stays false during playback');
    });
  });
});
