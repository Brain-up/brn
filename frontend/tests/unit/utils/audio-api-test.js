import audioApi, {
  BufferLoader,
  TIMINGS,
  toSeconds,
  createSource,
  toMilliseconds,
  createNoizeBuffer,
  createAudioContext,
} from 'brn/utils/audio-api';
import { module, test } from 'qunit';

module('Unit | Utility | audio-api', function() {
  // Replace this with your real tests.
  test('it works', function(assert) {
    let result = audioApi();
    assert.ok(result);
  });

  test('TIMINGS', (assert) => {
    assert.ok(TIMINGS.FAKE_AUDIO);
    assert.ok(TIMINGS.FAKE_AUDIO_STARTED);
    assert.ok(TIMINGS.FAKE_AUDIO_FINISHED);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION_STARTED);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION_FINISHED);
  });

  test('toSeconds', (assert) => {
    assert.equal(toSeconds(1000), 1);
  });

  test('toMilliseconds', (assert) => {
    assert.equal(toMilliseconds(1), 1000);
  });

  test('createSource', (assert) => {
    assert.ok(createSource(createAudioContext(), new AudioBuffer({
      length: 10,
      sampleRate: 8000
    })));
  });

  test('createNoizeBuffer', (assert) => {
    assert.ok(createNoizeBuffer(createAudioContext(), 10));
  });

  test('createAudioContext', (assert) => {
    assert.ok(createAudioContext());
  });

  test('BufferLoader', (assert) => {
    assert.ok(new BufferLoader());
  });
});
