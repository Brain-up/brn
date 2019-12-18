import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | study-history-saver', function(hooks) {
  setupTest(hooks);

  test('saves exercise history', function(assert) {
    let service = this.owner.lookup('service:study-history-saver');
    service.saveHistory = (storyToSave) => {
      assert.deepEqual(storyToSave, {
        startTime: storyToSave.startTime,
        endTime: storyToSave.endTime,
        repetitionIndex: 1,
        exerciseId: 12,
        tasksCount: 2,
      });
    };
    service.saveExerciseHistory({
      id: 12,
      startTime: new Date(),
      endTime: new Date(),
      tasks: [
        {
          repetitionCount: 1,
        },
        {
          repetitionCount: 1,
        },
      ],
    });
  });
});
