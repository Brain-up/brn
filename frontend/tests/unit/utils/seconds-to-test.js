import { secondsTo } from 'brn/utils/seconds-to';
import { module, test } from 'qunit';

module('Unit | Utility | secondsTo', function () {
  // Replace this with your real tests.
  test('it converts seconds to string', function (assert) {
    assert.equal(secondsTo(3600, 'h:m:s'), '01:00:00');
    assert.equal(secondsTo(3600, 'm:s'), '00:00');
    assert.equal(secondsTo(3660, 'm:s'), '01:00');

    assert.equal(secondsTo(2, 'h:m:s'), '00:00:02');
    assert.equal(secondsTo(2, 'm:s'), '00:02');

    assert.equal(secondsTo(1080000, 'h:m:s'), '300:00:00');

    assert.equal(secondsTo(55230, 'h:m:s'), '15:20:30');
  });
});
