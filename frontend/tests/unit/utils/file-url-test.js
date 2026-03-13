import { urlForImage, urlForAudio, setCloudBaseUrl, getCloudBaseUrl } from 'brn/utils/file-url';
import { module, test } from 'qunit';

module('Unit | Utility | file-url', function (hooks) {
  hooks.afterEach(function () {
    // Reset cloud base URL after each test
    setCloudBaseUrl('');
  });

  test('urlForImage returns null for null', function (assert) {
    assert.equal(urlForImage(null), null);
  });

  test('urlForImage returns null for undefined', function (assert) {
    assert.equal(urlForImage(undefined), null);
  });

  test('urlForImage returns http urls as-is', function (assert) {
    assert.equal(urlForImage('https://example.com/img.png'), 'https://example.com/img.png');
  });

  test('urlForImage prepends / for relative paths without cloud url', function (assert) {
    assert.equal(urlForImage('pictures/foo.png'), '/pictures/foo.png');
  });

  test('urlForImage prepends cloud base url for / paths', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com');
    assert.equal(
      urlForImage('/pictures/foo.png'),
      'https://brnup.s3.eu-north-1.amazonaws.com/pictures/foo.png',
    );
  });

  test('urlForImage prepends cloud base url for relative paths', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com');
    assert.equal(
      urlForImage('pictures/foo.png'),
      'https://brnup.s3.eu-north-1.amazonaws.com/pictures/foo.png',
    );
  });

  test('urlForImage does not double-prefix http urls when cloud url is set', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com');
    assert.equal(
      urlForImage('https://other.com/img.png'),
      'https://other.com/img.png',
    );
  });

  test('urlForAudio returns null for null', function (assert) {
    assert.equal(urlForAudio(null), null);
  });

  test('urlForAudio returns http urls as-is', function (assert) {
    assert.equal(urlForAudio('https://example.com/audio.mp3'), 'https://example.com/audio.mp3');
  });

  test('urlForAudio prepends cloud base url for / paths', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com');
    assert.equal(
      urlForAudio('/audio/test.mp3'),
      'https://brnup.s3.eu-north-1.amazonaws.com/audio/test.mp3',
    );
  });

  test('urlForAudio returns relative paths as-is without cloud url', function (assert) {
    assert.equal(urlForAudio('/audio/test.mp3'), '/audio/test.mp3');
  });

  test('urlForImage skips cloud resolution for /public/ paths', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com');
    assert.equal(urlForImage('/public/pictures/foo.png'), '/public/pictures/foo.png');
  });

  test('setCloudBaseUrl strips trailing slash', function (assert) {
    setCloudBaseUrl('https://brnup.s3.eu-north-1.amazonaws.com/');
    assert.equal(getCloudBaseUrl(), 'https://brnup.s3.eu-north-1.amazonaws.com');
  });
});
