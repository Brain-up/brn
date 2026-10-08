import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Unit | Helper | subtract', function (hooks) {
  setupRenderingTest(hooks);

  test('subtracts second from first', async function (assert) {
    this.set('a', 10);
    this.set('b', 3);
    await render(hbs`<span data-test>{{subtract this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('7');
  });

  test('defaults subtrahend to 0', async function (assert) {
    this.set('a', 5);
    await render(hbs`<span data-test>{{subtract this.a}}</span>`);
    assert.dom('[data-test]').hasText('5');
  });

  test('returns negative when subtrahend is larger', async function (assert) {
    this.set('a', 3);
    this.set('b', 10);
    await render(hbs`<span data-test>{{subtract this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('-7');
  });

  test('handles zero values', async function (assert) {
    this.set('a', 0);
    this.set('b', 0);
    await render(hbs`<span data-test>{{subtract this.a this.b}}</span>`);
    assert.dom('[data-test]').hasText('0');
  });
});
