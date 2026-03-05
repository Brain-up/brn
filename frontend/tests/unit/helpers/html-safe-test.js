import { module, test } from 'qunit';
import { htmlSafe } from '@ember/template';
import { isHTMLSafe } from '@ember/template';

module('Unit | Helper | html-safe', function () {
  test('htmlSafe returns a SafeString', function (assert) {
    const result = htmlSafe('<b>bold</b>');
    assert.true(isHTMLSafe(result));
  });

  test('SafeString toString returns the original string', function (assert) {
    const result = htmlSafe('<b>bold</b>');
    assert.strictEqual(result.toString(), '<b>bold</b>');
  });

  test('handles an empty string', function (assert) {
    const result = htmlSafe('');
    assert.strictEqual(result.toString(), '');
  });
});
