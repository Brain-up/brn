import { test } from '@playwright/test';
import { setupApiInterceptor } from './helpers/api-interceptor';
import { seedAuth } from './helpers/auth';
import { disableAnimationsBeforeLoad, expectScreenshot } from './helpers/visual';

test.describe('Authenticated pages', () => {
  test.beforeEach(async ({ page }) => {
    await setupApiInterceptor(page);
    await disableAnimationsBeforeLoad(page);
    await seedAuth(page);
  });

  test('series subgroups page', async ({ page }) => {
    await page.goto('/groups/1/series/1', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'series-subgroups-page');
  });

  test('group series page', async ({ page }) => {
    await page.goto('/groups/1', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'group-series-page');
  });

  test('groups page', async ({ page }) => {
    await page.goto('/groups', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'groups-page');
  });

  test('profile page', async ({ page }) => {
    await page.goto('/profile', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'profile-page');
  });

  test('statistics page', async ({ page }) => {
    await page.goto('/profile/statistics', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'statistics-page');
  });

  test('used resources page', async ({ page }) => {
    await page.goto('/used-resources', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'used-resources-page');
  });

  test('specialists page', async ({ page }) => {
    await page.goto('/specialists', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'specialists-page');
  });

  test('exercise page', async ({ page }) => {
    // Navigate to exercise — route auto-redirects to first task
    await page.goto('/groups/1/series/1/subgroup/1/exercise/1', {
      waitUntil: 'domcontentloaded',
    });
    // Wait for task player to load, then click play to start the exercise
    await page.waitForSelector('[data-test-start-task-button]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.click('[data-test-start-task-button]');
    await expectScreenshot(page, 'exercise-page', {
      contentSelector: '[data-test-task-answer]',
      // Mask the timer display to avoid flaky diffs from ticking seconds
      mask: [page.locator('[data-test-timer-display-value]')],
    });
  });

  test('not accessible page', async ({ page }) => {
    await page.goto('/not-accessable', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'not-accessible-page', {
      contentSelector: '.not-accessable',
    });
  });
});
