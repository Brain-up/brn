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
    function withAudio(owner, playing) {
      owner.register('service:audio', class extends Service { isPlaying = playing; });
      return owner.lookup('service:studying-timer');
    }

    test('pauses when audio is not playing', function (assert) {
      const timer = withAudio(this.owner, false);
      timer.maybeIdlePause();
      assert.true(timer.isPaused);
    });

    test('does not pause when audio is playing', function (assert) {
      const timer = withAudio(this.owner, true);
      timer.maybeIdlePause();
      assert.false(timer.isPaused);
    });

    test('user pause still works while audio plays', function (assert) {
      // Regression guard: only maybeIdlePause should defer to audio state;
      // direct pause() (timer button click) must remain unconditional.
      const timer = withAudio(this.owner, true);
      timer.pause();
      assert.true(timer.isPaused);
    });
  });

  module('resetIdle', function () {
    test('rearms the idle watcher via stop/start', function (assert) {
      const timer = this.owner.lookup('service:studying-timer');
      let stopCount = 0;
      let startCount = 0;
      timer.idleWatcher = {
        stop() {
          stopCount++;
        },
        start() {
          startCount++;
        },
      };
      timer.resetIdle();
      assert.strictEqual(stopCount, 1, 'idleWatcher.stop called once');
      assert.strictEqual(startCount, 1, 'idleWatcher.start called once');
    });

    test('does not toggle isPaused', function (assert) {
      // Regression guard: resetIdle must not clear a user-initiated pause.
      const timer = this.owner.lookup('service:studying-timer');
      timer.idleWatcher = { stop() {}, start() {} };
      timer.pause();
      timer.resetIdle();
      assert.true(timer.isPaused, 'user pause remains sticky after resetIdle');
    });

    test('is a no-op when idleWatcher is null', function (assert) {
      const timer = this.owner.lookup('service:studying-timer');
      timer.idleWatcher = null;
      try {
        timer.resetIdle();
        assert.ok(true, 'did not throw');
      } catch (_e) {
        assert.ok(false, 'resetIdle threw with null idleWatcher');
      }
    });
  });
});
