import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  outputDir: './e2e/test-results',
  snapshotPathTemplate: '{testDir}/__screenshots__/{projectName}/{testFilePath}/{arg}{ext}',

  fullyParallel: false,
  workers: 1,
  retries: 1,

  timeout: 30_000,
  expect: {
    toHaveScreenshot: {
      maxDiffPixelRatio: 0.01,
      threshold: 0.2,
    },
  },

  reporter: [
    ['html', { outputFolder: './e2e/playwright-report', open: 'never' }],
    ['list'],
  ],

  use: {
    baseURL: 'http://localhost:4200',
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
  },

  webServer: {
    command: 'npx vite --port 4200',
    port: 4200,
    timeout: 120_000,
    reuseExistingServer: !process.env.CI,
  },

  projects: [
    {
      name: 'desktop-chrome',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 },
      },
    },
    {
      name: 'mobile-chrome',
      use: {
        ...devices['Pixel 5'],
      },
    },
  ],
});
