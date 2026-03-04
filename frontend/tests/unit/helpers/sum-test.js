import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Unit | Helper | sum', function (hooks) {
  setupRenderingTest(hooks);

  test('adds two positive numbers', async function (assert) {
    this.set('a', 3);
    this.set('b', 4);
    await render(hbs`<span data-test>{{sum this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('7');
  });

  test('adds negative numbers', async function (assert) {
    this.set('a', -5);
    this.set('b', 3);
    await render(hbs`<span data-test>{{sum this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('-2');
  });

  test('adds zero', async function (assert) {
    this.set('a', 10);
    this.set('b', 0);
    await render(hbs`<span data-test>{{sum this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('10');
  });
});
