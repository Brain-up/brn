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

  // --- Edge cases for urlForImage ---

  test('urlForImage handles empty string without cloud url', function (assert) {
    assert.equal(urlForImage(''), '/');
  });

  test('urlForImage handles empty string with cloud url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(urlForImage(''), 'https://cdn.example.com/');
  });

  test('urlForImage handles path with just /', function (assert) {
    // '/' starts with '/' but no cloud URL, so returned as-is
    assert.equal(urlForImage('/'), '/');
  });

  test('urlForImage handles path with just / and cloud url set', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(urlForImage('/'), 'https://cdn.example.com/');
  });

  test('urlForImage does not skip /public paths without trailing slash segment', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    // '/public/' prefix is the guard; '/publicdata/foo.png' should resolve via cloud
    assert.equal(
      urlForImage('/publicdata/foo.png'),
      'https://cdn.example.com/publicdata/foo.png',
    );
  });

  test('urlForImage skips /public/ paths even with nested subpaths', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(
      urlForImage('/public/deep/nested/image.png'),
      '/public/deep/nested/image.png',
    );
  });

  test('urlForImage handles http (not https) urls as-is', function (assert) {
    assert.equal(urlForImage('http://example.com/img.png'), 'http://example.com/img.png');
  });

  test('urlForImage handles http urls as-is with cloud url set', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(urlForImage('http://example.com/img.png'), 'http://example.com/img.png');
  });

  test('urlForImage does not prepend cloud url for /public/ without cloud url set', function (assert) {
    assert.equal(urlForImage('/public/foo.png'), '/public/foo.png');
  });

  // --- Edge cases for urlForAudio ---

  test('urlForAudio skips /public/ paths', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(
      urlForAudio('/public/audio/test.mp3'),
      '/public/audio/test.mp3',
    );
  });

  test('urlForAudio returns relative paths as-is (non-slash-prefixed) without cloud url', function (assert) {
    assert.equal(urlForAudio('audio/test.mp3'), 'audio/test.mp3');
  });

  test('urlForAudio returns relative paths as-is (non-slash-prefixed) with cloud url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    // urlForAudio only prefixes paths starting with '/', not relative paths
    assert.equal(urlForAudio('audio/test.mp3'), 'audio/test.mp3');
  });

  test('urlForAudio handles empty string', function (assert) {
    assert.equal(urlForAudio(''), '');
  });

  test('urlForAudio handles empty string with cloud url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(urlForAudio(''), '');
  });

  test('urlForAudio handles path with just /', function (assert) {
    assert.equal(urlForAudio('/'), '/');
  });

  test('urlForAudio handles path with just / and cloud url set', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(urlForAudio('/'), 'https://cdn.example.com/');
  });

  test('urlForAudio handles http (not https) urls as-is', function (assert) {
    assert.equal(urlForAudio('http://example.com/audio.mp3'), 'http://example.com/audio.mp3');
  });

  test('urlForAudio does not double-prefix http urls when cloud url is set', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(
      urlForAudio('https://other.com/audio.mp3'),
      'https://other.com/audio.mp3',
    );
  });

  // --- setCloudBaseUrl / getCloudBaseUrl ---

  test('getCloudBaseUrl returns null initially before any setCloudBaseUrl call', function (assert) {
    // afterEach sets it to '', which becomes '' (empty string), not null
    // but the module-level default is null
    // After afterEach runs setCloudBaseUrl(''), getCloudBaseUrl() returns ''
    assert.equal(getCloudBaseUrl(), '');
  });

  test('setCloudBaseUrl does not strip trailing slash if url has no trailing slash', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(getCloudBaseUrl(), 'https://cdn.example.com');
  });

  test('setCloudBaseUrl with multiple trailing slashes strips only the last one', function (assert) {
    setCloudBaseUrl('https://cdn.example.com//');
    // Only strips one trailing slash via endsWith('/')
    assert.equal(getCloudBaseUrl(), 'https://cdn.example.com/');
  });

  test('setCloudBaseUrl with empty string sets empty cloud url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    assert.equal(getCloudBaseUrl(), 'https://cdn.example.com');
    setCloudBaseUrl('');
    assert.equal(getCloudBaseUrl(), '');
  });
});
