import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';
import { createStubTasks } from './test-support/helpers';

module('Integration | Component | exercise-navigation', function(hooks) {
  setupRenderingTest(hooks);

  test('renders navigation according to passed tasks', async function(assert) {
    const store = this.owner.lookup('service:store');
    this.set(
      'tasks',
      createStubTasks(store, [
        {
          order: '1',
          word: 'бал',
        },
        {
          order: '2',
          word: 'бум',
        },
        {
          order: '3',
          word: 'быль',
        },
      ]),
    );

    await render(hbs`<ExerciseNavigation
      @tasks={{this.tasks}}/>`);

    assert.equal(pageObject.navLinks.length, this.tasks.length);
    assert.deepEqual(pageObject.navLinks.mapBy('linkNum'), ['3', '2', '1']);
  });

  test('renders navigation according to passed tasks ( shuffled )', async function(assert) {
    const store = this.owner.lookup('service:store');
    this.set(
      'tasks',
      createStubTasks(store, [
        {
          order: '3',
          word: 'быль',
        },
        {
          order: '1',
          word: 'бал',
        },
        {
          order: '2',
          word: 'бум',
        },
      ]),
    );

    await render(hbs`<ExerciseNavigation
      @tasks={{this.tasks}}/>`);

    assert.equal(pageObject.navLinks.length, 3);
    assert.deepEqual(pageObject.navLinks.mapBy('linkNum'), ['3', '2', '1']);
  });
});
