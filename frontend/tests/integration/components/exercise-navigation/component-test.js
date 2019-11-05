import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import stubTasksInfo from './stub-tasks-info';
import pageObject from './page-object';

module('Integration | Component | exercise-navigation', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    const tasks = stubTasksInfo.map((taskInfo) =>
      store.createRecord('task', { ...taskInfo, id: taskInfo.order }),
    );
    this.set('tasks', tasks);

    await render(hbs`<ExerciseNavigation 
	@tasks={{this.tasks}}/>`);
  });

  test('renders navigation according to passed tasks', async function(assert) {
    assert
      .dom('[data-test-pagination-link]')
      .exists({ count: stubTasksInfo.length });
    assert.deepEqual(
      pageObject.navLinks.mapBy('linkText'),
      stubTasksInfo.mapBy('order'),
    );
  });
});
