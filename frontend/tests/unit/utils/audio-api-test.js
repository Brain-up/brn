import audioApi, {
  BufferLoader,
  TIMINGS,
  toSeconds,
  createSource,
  toMilliseconds,
  createNoizeBuffer,
  createAudioContext,
  audioBufferToWavBlob,
} from 'brn/utils/audio-api';
import { module, test } from 'qunit';

module('Unit | Utility | audio-api', function () {
  // Replace this with your real tests.
  test('it works', function (assert) {
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
    assert.ok(
      createSource(
        createAudioContext(),
        new AudioBuffer({
          length: 10,
          sampleRate: 8000,
        }),
      ),
    );
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

  test('audioBufferToWavBlob produces a valid WAV blob', (assert) => {
    const sampleRate = 8000;
    const length = 16;
    const buffer = new AudioBuffer({ length, sampleRate, numberOfChannels: 1 });
    const channel = buffer.getChannelData(0);
    for (let i = 0; i < length; i++) {
      channel[i] = i % 2 === 0 ? 1 : -1; // full-scale square wave
    }

    const blob = audioBufferToWavBlob(buffer);

    assert.strictEqual(blob.type, 'audio/wav', 'blob has the WAV mime type');
    // 44-byte header + length * channels * 2 bytes (16-bit PCM)
    assert.strictEqual(blob.size, 44 + length * 1 * 2, 'blob size matches header + PCM data');
  });

  test('audioBufferToWavBlob writes a correct RIFF/WAVE header', async (assert) => {
    const sampleRate = 8000;
    const length = 4;
    const buffer = new AudioBuffer({ length, sampleRate, numberOfChannels: 1 });

    const blob = audioBufferToWavBlob(buffer);
    const view = new DataView(await blob.arrayBuffer());
    const readTag = (offset) =>
      String.fromCharCode(
        view.getUint8(offset),
        view.getUint8(offset + 1),
        view.getUint8(offset + 2),
        view.getUint8(offset + 3),
      );

    assert.strictEqual(readTag(0), 'RIFF', 'starts with RIFF');
    assert.strictEqual(readTag(8), 'WAVE', 'declares WAVE format');
    assert.strictEqual(readTag(12), 'fmt ', 'has fmt chunk');
    assert.strictEqual(readTag(36), 'data', 'has data chunk');
    assert.strictEqual(view.getUint16(22, true), 1, 'channel count is written');
    assert.strictEqual(view.getUint32(24, true), sampleRate, 'sample rate is written');
    assert.strictEqual(view.getUint16(34, true), 16, 'bits per sample is 16');
  });
});
