import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

/**
 * Injects animation-disabling CSS via addInitScript so it's available
 * before page load. Call BEFORE page.goto().
 *
 * NOTE: We do NOT freeze Date.now() during boot — it breaks Ember's
 * internal timers. Instead we inject animation CSS only.
 */
export async function disableAnimationsBeforeLoad(page: Page): Promise<void> {
  await page.addInitScript(() => {
    // Patch Web Animation API — LoadingSpinner uses node.animate()
    const origAnimate = Element.prototype.animate;
    Element.prototype.animate = function (
      ...args: Parameters<typeof origAnimate>
    ) {
      const anim = origAnimate.apply(this, args);
      try {
        anim.finish();
      } catch {
        anim.cancel();
      }
      return anim;
    };

    // Mute all audio — prevent sound during test runs
    const origCreateElement = document.createElement.bind(document);
    // @ts-expect-error — monkey-patching createElement
    document.createElement = function (
      tagName: string,
      ...args: unknown[]
    ) {
      // @ts-expect-error — forwarding args
      const el = origCreateElement(tagName, ...args);
      if (tagName.toLowerCase() === 'audio' || tagName.toLowerCase() === 'video') {
        (el as HTMLMediaElement).muted = true;
        (el as HTMLMediaElement).volume = 0;
      }
      return el;
    };

    // Also patch HTMLMediaElement.prototype.play to ensure muting
    const origPlay = HTMLMediaElement.prototype.play;
    HTMLMediaElement.prototype.play = function () {
      this.muted = true;
      this.volume = 0;
      return origPlay.call(this);
    };

    // Patch AudioContext/webkitAudioContext to create at zero gain
    const OrigAudioContext = window.AudioContext || (window as any).webkitAudioContext;
    if (OrigAudioContext) {
      const origResume = OrigAudioContext.prototype.resume;
      OrigAudioContext.prototype.resume = function () {
        // Create a gain node set to 0 if not already done
        if (!(this as any).__muted) {
          const gain = this.createGain();
          gain.gain.value = 0;
          gain.connect(this.destination);
          (this as any).__muted = true;
        }
        return origResume.call(this);
      };
    }

    // Inject animation-killing CSS as early as possible
    const cssText = `
      *, *::before, *::after {
        animation-duration: 0s !important;
        animation-delay: 0s !important;
        transition-duration: 0s !important;
        transition-delay: 0s !important;
        animation-iteration-count: 1 !important;
      }
    `;

    if (document.head) {
      const style = document.createElement('style');
      style.textContent = cssText;
      document.head.appendChild(style);
    }

    // Also observe for head availability (SSR / early load)
    const observer = new MutationObserver(() => {
      if (document.head) {
        const style = document.createElement('style');
        style.textContent = cssText;
        document.head.appendChild(style);
        observer.disconnect();
      }
    });
    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    });
  });
}

/**
 * Waits for the Ember application to be fully rendered.
 * Uses footer.c-footer as the reliable indicator that the app has booted
 * and rendered the application template.
 */
export async function waitForAppSettled(
  page: Page,
  contentSelector?: string,
): Promise<void> {
  // Wait for the Ember app to boot — footer is always rendered
  await page.waitForSelector('footer.c-footer', {
    state: 'visible',
    timeout: 20_000,
  });

  // Wait for route-specific content if provided
  if (contentSelector) {
    await page.waitForSelector(contentSelector, {
      state: 'visible',
      timeout: 15_000,
    });
  }

  // Wait for loading indicators to disappear
  const spinnerCount = await page.locator('.c-loading-spinner').count();
  if (spinnerCount > 0) {
    await page
      .locator('.c-loading-spinner')
      .first()
      .waitFor({ state: 'hidden', timeout: 10_000 })
      .catch(() => {});
  }

  const skeletonCount = await page.locator('.skeleton-page').count();
  if (skeletonCount > 0) {
    await page
      .locator('.skeleton-page')
      .first()
      .waitFor({ state: 'hidden', timeout: 10_000 })
      .catch(() => {});
  }

  // Wait for fonts to be loaded
  await page.evaluate(() => document.fonts.ready);

  // Short settle pause for final renders
  await page.waitForTimeout(500);
}

/**
 * Takes a full-page screenshot and compares against baseline.
 * @param contentSelector - CSS selector for route-specific content to wait for
 */
export async function expectScreenshot(
  page: Page,
  name: string,
  options?: {
    fullPage?: boolean;
    mask?: ReturnType<Page['locator']>[];
    contentSelector?: string;
  },
): Promise<void> {
  await waitForAppSettled(page, options?.contentSelector);

  await expect(page).toHaveScreenshot(`${name}.png`, {
    fullPage: options?.fullPage ?? true,
    animations: 'disabled',
    mask: options?.mask,
  });
}
