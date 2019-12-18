import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import StudyHistory from 'brn/models/study-history';

module('Unit | Model | study history', function(hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function(assert) {
    let model = StudyHistory.generate({
      userId: 1,
      startTime: new Date(),
      endTime: new Date(),
      exerciseId: 1,
      tasksCount: 1,
      repetitionIndex: 0.1,
    });
    assert.ok(model);
  });
});
