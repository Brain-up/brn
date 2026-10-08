import { test } from '@playwright/test';
import { setupApiInterceptor } from './helpers/api-interceptor';
import { seedLocale } from './helpers/auth';
import { disableAnimationsBeforeLoad, expectScreenshot } from './helpers/visual';

test.describe('Public pages', () => {
  test.beforeEach(async ({ page }) => {
    await setupApiInterceptor(page);
    await seedLocale(page);
    await disableAnimationsBeforeLoad(page);
  });

  test('landing page', async ({ page }) => {
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'landing-page', {
      contentSelector: '[data-test-registration-form]',
    });
  });

  test('registration page', async ({ page }) => {
    // Navigate via landing → header Registration button → registration page
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('[data-test-registration-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.click('[data-test-registration-form]');
    await expectScreenshot(page, 'registration-page', {
      contentSelector: '[data-test-submit-form]',
    });
  });

  test('login page', async ({ page }) => {
    // Navigate via landing → registration → Sign In tab → login page
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('[data-test-registration-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.click('[data-test-registration-form]');
    await page.waitForSelector('[data-test-submit-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    // Click the Sign In tab on the registration page
    await page.click('a[href="/login"]');
    await expectScreenshot(page, 'login-page', {
      contentSelector: '[data-test-submit-form]',
    });
  });

  test('password recovery page', async ({ page }) => {
    // Navigate via landing → registration → Sign In tab → forgot password
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('[data-test-registration-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.click('[data-test-registration-form]');
    await page.waitForSelector('[data-test-submit-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.click('a[href="/login"]');
    await page.waitForSelector('[data-test-submit-form]', {
      state: 'visible',
      timeout: 15_000,
    });
    await page.getByRole('link', { name: /forgot/i }).click();
    await expectScreenshot(page, 'password-recovery-page', {
      contentSelector: 'form',
    });
  });

  test('user agreement page', async ({ page }) => {
    await page.goto('/user-agreement', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'user-agreement-page', {
      contentSelector: 'h3',
    });
  });

  test('contact page', async ({ page }) => {
    await page.goto('/contact', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'contact-page', {
      contentSelector: 'section',
    });
  });

  test('description page', async ({ page }) => {
    await page.goto('/description', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'description-page', {
      contentSelector: 'section',
    });
  });

  test('description developers page', async ({ page }) => {
    await page.goto('/description/developers', {
      waitUntil: 'domcontentloaded',
    });
    await expectScreenshot(page, 'description-developers-page', {
      contentSelector: 'section',
    });
  });

  test('contributors page', async ({ page }) => {
    await page.goto('/contributors', { waitUntil: 'domcontentloaded' });
    await expectScreenshot(page, 'contributors-page', {
      contentSelector: 'section',
    });
  });

});
