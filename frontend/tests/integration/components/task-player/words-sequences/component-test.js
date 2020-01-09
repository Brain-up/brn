import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import data from './test-support/data-storage';
import pageObject from './test-support/page-object';

module('Integration | Component | words-seq-task-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    this.set('model', store.createRecord('task/words-sequences'));

    this.model.setProperties(data.task);

    await render(hbs`<TaskPlayer
      @task={{this.model}}
    />`);
  });

  test('it shows all the words', async function(assert) {
    const pageWords = pageObject.buttons.mapBy('word');

    Object.values(data.task.answerOptions)
      .reduce((array, subArray) => {
        array = array.concat(subArray);
        return array;
      }, [])
      .mapBy('word')
      .forEach((word) => {
        assert.ok(pageWords.includes(word), `word "${word}" is present`);
      });
  });
});
