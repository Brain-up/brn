import type { Page } from '@playwright/test';
import {
  mockUser,
  mockGroups,
  mockSeries,
  mockSubgroups,
  mockExercises,
  mockExerciseWithTasks,
  mockContributors,
  emptyResponse,
  emptyObject,
} from '../fixtures/api-mocks';
import { FAKE_JWT } from '../fixtures/fake-jwt';

interface RouteHandler {
  method: string;
  pattern: RegExp;
  handler: (params: Record<string, string>, url: URL) => unknown;
}

function buildRoutes(): RouteHandler[] {
  return [
    {
      method: 'GET',
      pattern: /\/api\/users\/current$/,
      handler: () => mockUser,
    },
    {
      method: 'PATCH',
      pattern: /\/api\/users\/current$/,
      handler: () => emptyResponse,
    },
    {
      method: 'GET',
      pattern: /\/api\/groups$/,
      handler: () => mockGroups,
    },
    {
      method: 'GET',
      pattern: /\/api\/groups\/(\d+)$/,
      handler: (params) => {
        const group = mockGroups.data.find((g) => g.id === params.id);
        return { data: group ?? {} };
      },
    },
    {
      method: 'GET',
      pattern: /\/api\/series$/,
      handler: (_params, url) => {
        const groupId = url.searchParams.get('groupId');
        if (groupId) {
          const group = mockGroups.data.find((g) => g.id === groupId);
          const seriesIds = group?.series ?? [];
          return {
            data: mockSeries.data.filter((s) => seriesIds.includes(s.id)),
          };
        }
        return mockSeries;
      },
    },
    {
      method: 'GET',
      pattern: /\/api\/series\/(\d+)$/,
      handler: (params) => {
        const series = mockSeries.data.find((s) => s.id === params.id);
        return { data: series ?? {} };
      },
    },
    {
      method: 'GET',
      pattern: /\/api\/subgroups$/,
      handler: (_params, url) => {
        const seriesId = url.searchParams.get('seriesId');
        if (seriesId) {
          return {
            data: mockSubgroups.data.filter((sg) => sg.series === seriesId),
          };
        }
        return mockSubgroups;
      },
    },
    {
      method: 'GET',
      pattern: /\/api\/exercises$/,
      handler: (_params, url) => {
        const subGroupId = url.searchParams.get('subGroupId');
        if (subGroupId) {
          const subgroup = mockSubgroups.data.find(
            (sg) => sg.id === subGroupId,
          );
          const exerciseIds = subgroup?.exercises ?? [];
          return {
            data: mockExercises.data.filter((e) =>
              exerciseIds.includes(e.id),
            ),
          };
        }
        return mockExercises;
      },
    },
    {
      method: 'GET',
      pattern: /\/api\/exercises\/(\d+)$/,
      handler: (params) => {
        return mockExerciseWithTasks(params.id);
      },
    },
    {
      method: 'POST',
      pattern: /\/api\/exercises\/byIds$/,
      handler: () => ({
        data: mockExercises.data
          .filter((e) => e.available)
          .map((e) => parseInt(e.id, 10)),
      }),
    },
    {
      method: 'GET',
      pattern: /\/api\/tasks$/,
      handler: () => emptyResponse,
    },
    {
      method: 'GET',
      pattern: /\/api\/tasks\/(\d+)$/,
      handler: () => emptyObject,
    },
    {
      method: 'POST',
      pattern: /\/api\/study-history$/,
      handler: () => ({ id: '1' }),
    },
    {
      method: 'GET',
      pattern: /\/api\/statistics\/subgroups$/,
      handler: () => emptyResponse,
    },
    {
      method: 'GET',
      pattern: /\/api\/v2\/statistics\/study\/(week|year|day)$/,
      handler: () => emptyResponse,
    },
    {
      method: 'GET',
      pattern: /\/api\/v1\/symbols\/search$/,
      handler: () => [],
    },
    {
      method: 'GET',
      pattern: /\/api\/pictograms\/.+\/(bestsearch|search)\/.+$/,
      handler: () => [],
    },
    {
      method: 'GET',
      pattern: /\/api\/contributors$/,
      handler: () => mockContributors,
    },
  ];
}

/** Domains to block (analytics only) */
const blockedDomains = [
  'mc.yandex.ru',
  'googletagmanager.com',
  'google-analytics.com',
  'www.google-analytics.com',
];

/**
 * Mock Firebase token refresh response.
 * Firebase SDK calls securetoken.googleapis.com/v1/token to refresh auth tokens.
 */
const firebaseTokenResponse = {
  access_token: FAKE_JWT,
  expires_in: '3600',
  token_type: 'Bearer',
  refresh_token: 'fake-refresh-token',
  id_token: FAKE_JWT,
  user_id: 'test-uid',
  project_id: 'brainupspb',
};

/**
 * Mock Firebase user lookup response.
 * Firebase SDK calls identitytoolkit.googleapis.com/v1/accounts:lookup
 */
const firebaseUserLookup = {
  users: [
    {
      localId: 'test-uid',
      email: 'admin@admin.com',
      emailVerified: true,
      displayName: 'admin',
      providerUserInfo: [],
      photoUrl: '',
      passwordHash: '',
      lastLoginAt: '1700000000000',
      createdAt: '1600000000000',
      lastRefreshAt: '2025-01-15T12:00:00.000Z',
    },
  ],
};

export async function setupApiInterceptor(page: Page): Promise<void> {
  const routes = buildRoutes();

  // Block analytics
  for (const domain of blockedDomains) {
    await page.route(`**/${domain}/**`, (route) => route.abort());
    await page.route(`**${domain}**`, (route) => route.abort());
  }

  // Mock Firebase auth API (token refresh)
  await page.route('**/securetoken.googleapis.com/**', (route) => {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(firebaseTokenResponse),
    });
  });

  // Mock Firebase auth API (user lookup / accounts)
  // Firebase v8 uses www.googleapis.com/identitytoolkit/v3/relyingparty/
  await page.route('**/identitytoolkit/**', (route) => {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(firebaseUserLookup),
    });
  });

  // Mock Firebase installations API
  await page.route('**/firebaseinstallations.googleapis.com/**', (route) => {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        name: 'projects/brainupspb/installations/fake-fid',
        fid: 'fake-fid',
        refreshToken: 'fake-refresh',
        authToken: { token: 'fake-auth-token', expiresIn: '604800s' },
      }),
    });
  });

  // Intercept /api/* requests
  await page.route('**/api/**', (route, request) => {
    const url = new URL(request.url());
    const method = request.method().toUpperCase();

    for (const r of routes) {
      if (r.method !== method) continue;

      const match = url.pathname.match(r.pattern);
      if (!match) continue;

      // Extract numeric capture group as "id" param
      const params: Record<string, string> = {};
      if (match[1]) params.id = match[1];

      const body = r.handler(params, url);
      return route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(body),
      });
    }

    // Unhandled API route — return empty 200
    console.warn(`[e2e] Unhandled ${method} ${url.pathname}`);
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ data: [] }),
    });
  });
}
