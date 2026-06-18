import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env['E2E_BASE_URL'] || 'http://localhost:4200';
const apiBaseURL = process.env['E2E_API_BASE_URL'] || 'http://localhost:8080';

export default defineConfig({
  testDir: './e2e',
  testMatch: 'specs/**/*.spec.ts',
  timeout: 60_000,
  expect: {
    timeout: 10_000
  },
  fullyParallel: false,
  retries: process.env['CI'] ? 1 : 0,
  reporter: [['list']],
  use: {
    baseURL,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  },
  metadata: {
    apiBaseURL
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
});
