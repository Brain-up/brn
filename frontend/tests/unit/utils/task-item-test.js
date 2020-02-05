import { TaskItem } from 'brn/utils/task-item';
import { module, test } from 'qunit';

module('Unit | Utility | task-item', function() {
  // Replace this with your real tests.
  test('it works', function(assert) {
    let result = new TaskItem();
    assert.ok(result);
    assert.ok('isCompleted' in result);
    assert.ok('canInteract' in result);
    assert.ok('order' in result);
    assert.ok('completedInCurrentCycle' in result);
    assert.ok('nextAttempt' in result);
  });
});
