import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { run } from '@ember/runloop';
import GroupNavigation from 'brn/components/group-navigation';

module('Integration | Component | group-navigation', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders links from @group.series', async function (assert) {
    const store = this.owner.lookup('service:store');
    this.owner.setupRouter();
    const group = run(() => {
      const group = store.createRecord('group');
      store.createRecord('series', { group, name: 1 });
      store.createRecord('series', { group });
      store.createRecord('series', { group });
      return group;
    });
    this.setProperties({ group });
    const self = this;

    await render(<template><GroupNavigation @group={{self.group}} /></template>);
    assert.equal(
      this.element.querySelectorAll('a').length,
      3,
      '3 links',
    );
  });

  test('renders tabs when @series is passed directly (the fix path)', async function (assert) {
    this.owner.setupRouter();
    const series = [
      { id: '1', name: 'Words' },
      { id: '2', name: 'Phrases' },
      { id: '3', name: 'Sentences' },
    ];
    const group = { id: '10', name: 'Test Group' };

    await render(
      <template><GroupNavigation @group={{group}} @series={{series}} /></template>
    );

    const links = this.element.querySelectorAll('a');
    assert.strictEqual(links.length, 3, 'renders one tab per series item');

    // Verify names are rendered in the tabs
    const tabTexts = [...links].map((a) => a.textContent.trim());
    assert.true(tabTexts.includes('Words'), 'renders "Words" tab');
    assert.true(tabTexts.includes('Phrases'), 'renders "Phrases" tab');
    assert.true(tabTexts.includes('Sentences'), 'renders "Sentences" tab');
  });

  test('renders zero tabs when @series is an empty array', async function (assert) {
    this.owner.setupRouter();
    const series = [];
    const group = { id: '10', name: 'Test Group' };

    await render(
      <template><GroupNavigation @group={{group}} @series={{series}} /></template>
    );

    const links = this.element.querySelectorAll('a');
    assert.strictEqual(links.length, 0, 'renders no tabs when series is empty');
    // The container should still exist
    assert.dom('.hs-container').exists('container is still rendered');
  });

  test('renders zero tabs when neither @series nor @group.series is provided', async function (assert) {
    this.owner.setupRouter();

    await render(
      <template><GroupNavigation /></template>
    );

    const links = this.element.querySelectorAll('a');
    assert.strictEqual(links.length, 0, 'renders no tabs with no data');
  });

  test('@series takes precedence over @group.series', async function (assert) {
    const store = this.owner.lookup('service:store');
    this.owner.setupRouter();

    // Create a group with 2 series via store (this populates @group.series)
    const group = run(() => {
      const g = store.createRecord('group');
      store.createRecord('series', { group: g, name: 'Store Series 1' });
      store.createRecord('series', { group: g, name: 'Store Series 2' });
      return g;
    });

    // Pass 3 different series via the @series arg
    const seriesArg = [
      { id: '100', name: 'Arg Series A' },
      { id: '101', name: 'Arg Series B' },
      { id: '102', name: 'Arg Series C' },
    ];

    await render(
      <template><GroupNavigation @group={{group}} @series={{seriesArg}} /></template>
    );

    const links = this.element.querySelectorAll('a');
    assert.strictEqual(links.length, 3, '@series arg wins over @group.series');

    const tabTexts = [...links].map((a) => a.textContent.trim());
    assert.true(tabTexts.includes('Arg Series A'), 'renders from @series arg, not @group.series');
  });

  test('tabs are sorted by id', async function (assert) {
    this.owner.setupRouter();
    // Pass series in non-sorted order
    const series = [
      { id: '3', name: 'Third' },
      { id: '1', name: 'First' },
      { id: '2', name: 'Second' },
    ];
    const group = { id: '10', name: 'Test Group' };

    await render(
      <template><GroupNavigation @group={{group}} @series={{series}} /></template>
    );

    const links = this.element.querySelectorAll('a');
    assert.strictEqual(links.length, 3, 'all tabs render');

    const tabTexts = [...links].map((a) => a.textContent.trim());
    assert.strictEqual(tabTexts[0], 'First', 'first tab is id=1');
    assert.strictEqual(tabTexts[1], 'Second', 'second tab is id=2');
    assert.strictEqual(tabTexts[2], 'Third', 'third tab is id=3');
  });

  test('tabs have data-test-active-link attribute with series name', async function (assert) {
    this.owner.setupRouter();
    const series = [
      { id: '1', name: 'Words' },
    ];
    const group = { id: '10', name: 'Test Group' };

    await render(
      <template><GroupNavigation @group={{group}} @series={{series}} /></template>
    );

    assert.dom('[data-test-active-link="Words"]').exists('tab has data-test attribute with series name');
  });
});
