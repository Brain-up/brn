import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { isArray } from '@ember/array';
import Service from '@ember/service';

module('Unit | Controller | description', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let controller = this.owner.lookup('controller:description');
    assert.ok(controller);
  });

  test('gets persons data', function (assert) {
    this.owner.register(
      'service:persons',
      class MockService extends Service {
        get persons() {
          return {
            test: [0, 1, 2],
          };
        }
      },
    );
    let controller = this.owner.lookup('controller:description');
    assert.ok(
      isArray(controller.persons.test) && controller.persons.test.length === 3,
    );
  });
});
