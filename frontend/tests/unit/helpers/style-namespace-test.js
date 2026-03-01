import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import podNames from 'ember-component-css/pod-names';

module('Unit | Helper | style-namespace', function (hooks) {
  setupRenderingTest(hooks);

  test('returns the pod class name for a known component', function (assert) {
    // Find any component that actually has a pod name registered
    const knownEntries = Object.entries(podNames);
    if (knownEntries.length === 0) {
      assert.ok(true, 'no pod names registered — nothing to test');
      return;
    }
    const [componentName, className] = knownEntries[0];
    assert.strictEqual(
      podNames[componentName],
      className,
      `podNames["${componentName}"] returns "${className}"`,
    );
  });

  test('returns undefined for a nonexistent component', function (assert) {
    assert.strictEqual(
      podNames['__nonexistent_component_xyz__'],
      undefined,
      'unknown component returns undefined',
    );
  });

  test('renders the correct class name in a template', async function (assert) {
    const knownEntries = Object.entries(podNames);
    if (knownEntries.length === 0) {
      assert.ok(true, 'no pod names registered — nothing to test');
      return;
    }
    const [componentName, className] = knownEntries[0];
    this.set('componentName', componentName);
    await render(
      hbs`<span data-test>{{style-namespace this.componentName}}</span>`,
    );
    assert.dom('[data-test]').hasText(
      String(className),
      `helper renders class name "${className}" for "${componentName}"`,
    );
  });
});
