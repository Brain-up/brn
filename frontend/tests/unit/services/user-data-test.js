import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | user-data', function (hooks) {
  setupTest(hooks);

  hooks.beforeEach(function () {
    localStorage.removeItem('audioPlaybackRate');
  });

  hooks.afterEach(function () {
    localStorage.removeItem('audioPlaybackRate');
  });

  test('it exists', function (assert) {
    let service = this.owner.lookup('service:user-data');
    assert.ok(service);
  });

  test('audioPlaybackRate defaults to 1 when nothing is stored', function (assert) {
    const service = this.owner.lookup('service:user-data');
    assert.strictEqual(service.audioPlaybackRate, 1, 'default rate is 1');
  });

  test('setAudioPlaybackRate updates the value and persists it', function (assert) {
    const service = this.owner.lookup('service:user-data');
    service.setAudioPlaybackRate(0.5);
    assert.strictEqual(service.audioPlaybackRate, 0.5, 'value updated');
    assert.strictEqual(
      localStorage.getItem('audioPlaybackRate'),
      '0.5',
      'value persisted to localStorage',
    );
  });

  test('a persisted rate is restored on the next service instance', function (assert) {
    localStorage.setItem('audioPlaybackRate', '0.75');
    // setupTest creates a fresh owner per test, so a fresh lookup reflects
    // what initialization reads from storage.
    const service = this.owner.factoryFor('service:user-data').create();
    assert.strictEqual(service.audioPlaybackRate, 0.75, 'restored from storage');
  });

  test('an invalid/unknown persisted rate falls back to 1', function (assert) {
    localStorage.setItem('audioPlaybackRate', 'not-a-number');
    const service = this.owner.factoryFor('service:user-data').create();
    assert.strictEqual(service.audioPlaybackRate, 1, 'falls back to default');

    localStorage.setItem('audioPlaybackRate', '3'); // not one of the presets
    const service2 = this.owner.factoryFor('service:user-data').create();
    assert.strictEqual(
      service2.audioPlaybackRate,
      1,
      'unknown preset falls back to default',
    );
  });
});
