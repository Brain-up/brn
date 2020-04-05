import {urlForImage, urlForAudio} from 'brn/utils/file-url';
import { module, test } from 'qunit';

module('Unit | Utility | file-url', function() {

  // Replace this with your real tests.
  test('it works: urlForImage', function(assert) {
    let result = urlForImage('foo');
    assert.ok(result);
  });

  test('it works: urlForAudio', function(assert) {
    let result = urlForAudio('bar');
    assert.ok(result);
  });

    // Replace this with your real tests.
    test('it works: urlForImage handle null', function(assert) {
      let result = urlForImage(null);
      assert.equal(result, null);
    });
  
    test('it works: urlForAudio handle null', function(assert) {
      let result = urlForAudio(null);
      assert.equal(result, null);
    });
});
