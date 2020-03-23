import audioApi, {
  BufferLoader,
  TIMINGS,
  toSeconds,
  toMilliseconds,
  createAudioContext,
} from 'brn/utils/audio-api';
import { module, test } from 'qunit';
import { createSource } from '../../../app/utils/audio-api';

module('Unit | Utility | audio-api', function() {
  // Replace this with your real tests.
  test('it works', function(assert) {
    let result = audioApi();
    assert.ok(result);
  });

  test('TIMINGS', () => {
    assert.ok(TIMINGS.FAKE_AUDIO);
    assert.ok(TIMINGS.FAKE_AUDIO_STARTED);
    assert.ok(TIMINGS.FAKE_AUDIO_FINISHED);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION_STARTED);
    assert.ok(TIMINGS.SUCCESS_ANSWER_NOTIFICATION_FINISHED);
  });

  test('toSeconds', () => {
    assert.equal(toSeconds(1000), 1);
  });

  test('toMilliseconds', () => {
    assert.equal(toMilliseconds(1), 1000);
  });

  test('createSource', () => {
    assert.ok(createSource(createAudioContext(), {}));
  });

  test('createAudioContext', () => {
    assert.ok(createAudioContext());
  });

  test('BufferLoader', ()=> {
    assert.ok(new BufferLoader());
  });
});
