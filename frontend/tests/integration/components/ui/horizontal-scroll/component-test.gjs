import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click, settled } from '@ember/test-helpers';
import UiHorizontalScroll from 'brn/components/ui/horizontal-scroll';

function forceScrollProperties(container, { scrollWidth, offsetWidth, scrollLeft }) {
  if (scrollWidth !== undefined) {
    Object.defineProperty(container, 'scrollWidth', {
      get: () => scrollWidth,
      configurable: true,
    });
  }
  if (offsetWidth !== undefined) {
    Object.defineProperty(container, 'offsetWidth', {
      get: () => offsetWidth,
      configurable: true,
    });
  }
  if (scrollLeft !== undefined) {
    Object.defineProperty(container, 'scrollLeft', {
      get: () => scrollLeft,
      set: () => {},
      configurable: true,
    });
  }
}

module('Integration | Component | ui/horizontal-scroll', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders yielded content inside a scrollable list', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item 1</li>
          <li>Item 2</li>
        </UiHorizontalScroll>
      </template>,
    );

    assert.dom('.hs-container').exists('renders the hs-container wrapper');
    assert.dom('.hs.no-scrollbar').exists('renders the scrollable list');
    assert.dom('.hs.no-scrollbar li').exists({ count: 2 }, 'yields content inside the list');
  });

  test('it spreads attributes onto the root element', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll data-test-my-scroll class="extra-class">
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    assert.dom('[data-test-my-scroll]').exists('spreads data attributes');
    assert.dom('.hs-container.extra-class').exists('spreads class attribute');
  });

  test('scroll buttons are hidden when content does not overflow', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Short</li>
        </UiHorizontalScroll>
      </template>,
    );

    assert
      .dom('[aria-label="Scroll left"]')
      .doesNotExist('left scroll button is hidden');
    assert
      .dom('[aria-label="Scroll right"]')
      .doesNotExist('right scroll button is hidden');
    assert
      .dom('.scroll-fade')
      .doesNotExist('no scroll fade gradients');
  });

  test('right scroll button appears when content overflows', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 0 });

    // Dispatch scroll to trigger debounced recomputation
    container.dispatchEvent(new Event('scroll'));
    await settled();

    assert
      .dom('[aria-label="Scroll right"]')
      .exists('right scroll button appears for overflowing content');
    assert
      .dom('.scroll-fade--right')
      .exists('right fade gradient appears');
    assert
      .dom('[aria-label="Scroll left"]')
      .doesNotExist('left scroll button is hidden at scroll start');
  });

  test('left scroll button appears after scrolling right', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 150 });

    container.dispatchEvent(new Event('scroll'));
    await settled();

    assert
      .dom('[aria-label="Scroll left"]')
      .exists('left scroll button appears after scrolling');
    assert
      .dom('.scroll-fade--left')
      .exists('left fade gradient appears');
  });

  test('both scroll buttons appear when scrolled to the middle', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 400 });

    container.dispatchEvent(new Event('scroll'));
    await settled();

    assert
      .dom('[aria-label="Scroll left"]')
      .exists('left scroll button is visible');
    assert
      .dom('[aria-label="Scroll right"]')
      .exists('right scroll button is visible');
  });

  test('right scroll button hides when scrolled to the end', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    // scrollLeft + offsetWidth >= scrollWidth means we've reached the end
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 800 });

    container.dispatchEvent(new Event('scroll'));
    await settled();

    assert
      .dom('[aria-label="Scroll left"]')
      .exists('left button is still visible');
    assert
      .dom('[aria-label="Scroll right"]')
      .doesNotExist('right button hides at the end');
  });

  test('clicking right scroll button calls scrollTo', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 0 });

    let scrollToArgs = null;
    container.scrollTo = (args) => { scrollToArgs = args; };

    container.dispatchEvent(new Event('scroll'));
    await settled();

    await click('[aria-label="Scroll right"]');

    assert.deepEqual(scrollToArgs, { top: 0, left: 150, behavior: 'smooth' }, 'scrollTo called with offset +150');
  });

  test('clicking left scroll button calls scrollTo backwards', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll>
          <li>Item</li>
        </UiHorizontalScroll>
      </template>,
    );

    const container = this.element.querySelector('.hs.no-scrollbar');
    forceScrollProperties(container, { scrollWidth: 1000, offsetWidth: 200, scrollLeft: 300 });

    let scrollToArgs = null;
    container.scrollTo = (args) => { scrollToArgs = args; };

    container.dispatchEvent(new Event('scroll'));
    await settled();

    await click('[aria-label="Scroll left"]');

    assert.deepEqual(scrollToArgs, { top: 0, left: 150, behavior: 'smooth' }, 'scrollTo called with offset -150');
  });

  test('renders with no yielded content', async function (assert) {
    await render(
      <template>
        <UiHorizontalScroll />
      </template>,
    );

    assert.dom('.hs-container').exists('renders empty container');
    assert.dom('.hs.no-scrollbar').exists('renders the scrollable list');
    assert
      .dom('[aria-label="Scroll left"]')
      .doesNotExist('no left button for empty content');
    assert
      .dom('[aria-label="Scroll right"]')
      .doesNotExist('no right button for empty content');
  });
});
