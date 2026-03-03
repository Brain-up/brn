/**
 * Test helper using @mswjs/interceptors for reliable fetch interception.
 *
 * Uses FetchInterceptor (same mechanism as MSW's setupServer internally).
 * App-level @ember/test-waiters integration in services/network.ts ensures
 * settled() waits for all pending requests.
 *
 * Provides a mirage-compatible server.get/post/put/delete API.
 * Handler signature: `server.get('path', (request) => response)`
 * where request = { params, queryParams, requestBody }.
 */
import { FetchInterceptor } from '@mswjs/interceptors/fetch';

// ─── Path matching (with cache) ─────────────────────────────────────────────

const pathCache = new Map();

function compilePath(pattern) {
  if (pathCache.has(pattern)) return pathCache.get(pattern);
  const paramNames = [];
  const regexStr = pattern.replace(/:([^/]+)/g, (_match, name) => {
    paramNames.push(name);
    return '([^/]+)';
  });
  const result = { regex: new RegExp(`^${regexStr}$`), paramNames };
  pathCache.set(pattern, result);
  return result;
}

function matchPath(pattern, pathname) {
  const { regex, paramNames } = compilePath(pattern);
  const match = pathname.match(regex);
  if (!match) return null;
  const params = {};
  paramNames.forEach((name, i) => {
    params[name] = match[i + 1];
  });
  return params;
}

// ─── Handler registry ────────────────────────────────────────────────────────

let runtimeHandlers = [];

const defaultHandlers = [
  {
    method: 'GET',
    path: '/api/users/current',
    handler: () => ({
      data: [
        {
          id: '1',
          name: 'First-Name Last-Name',
          email: 'em@il',
          bornYear: 2000,
          gender: 'MALE',
          active: true,
          avatar: '1',
          roles: ['ROLE_USER'],
        },
      ],
      errors: [],
      meta: [],
    }),
  },
  {
    method: 'GET',
    path: '/api/v2/statistics/study/week',
    handler: () => ({ data: [] }),
  },
  {
    method: 'GET',
    path: '/api/v2/statistics/study/year',
    handler: () => ({ data: [] }),
  },
  { method: 'GET', path: '/api/groups', handler: () => ({ data: [] }) },
  {
    method: 'GET',
    path: '/api/groups/:id',
    handler: (request) => ({
      data: {
        id: request.params.id,
        locale: 'ru-ru',
        name: '',
        description: '',
        series: [],
      },
    }),
  },
  { method: 'GET', path: '/api/subgroups', handler: () => ({ data: [] }) },
  { method: 'GET', path: '/api/series', handler: () => ({ data: [] }) },
  { method: 'GET', path: '/api/series/:id', handler: () => ({ data: {} }) },
  { method: 'GET', path: '/api/exercises', handler: () => ({ data: [] }) },
  {
    method: 'GET',
    path: '/api/exercises/:id',
    handler: () => ({ data: {} }),
  },
  { method: 'GET', path: '/api/tasks', handler: () => ({ data: [] }) },
  {
    method: 'GET',
    path: '/api/tasks/:id',
    handler: (request) => ({
      data: {
        id: Number(request.params.id),
        serialNumber: 0,
        name: '',
        level: 0,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
        shouldBeWithPictures: true,
        answerOptions: [],
      },
    }),
  },
  {
    method: 'POST',
    path: '/api/study-history',
    handler: () => ({ id: '1' }),
  },
  {
    method: 'POST',
    path: '/api/exercises/byIds',
    handler: () => ({ data: [] }),
  },
  {
    method: 'GET',
    path: '/api/statistics/subgroups',
    handler: () => ({ data: [] }),
  },
  {
    method: 'GET',
    path: '/api/v2/statistics/study/day',
    handler: () => ({ data: [] }),
  },
  {
    method: 'PATCH',
    path: '/api/users/current',
    handler: () => ({ data: [] }),
  },
  {
    method: 'GET',
    path: '/api/contributors',
    handler: () => ({ data: [] }),
  },
];

// ─── Interceptor singleton ───────────────────────────────────────────────────

let interceptor;
let interceptorApplied = false;

function findHandler(method, pathname) {
  for (const entry of runtimeHandlers) {
    if (entry.method !== method) continue;
    const params = matchPath(entry.path, pathname);
    if (params) return { handler: entry.handler, params };
  }
  for (const entry of defaultHandlers) {
    if (entry.method !== method) continue;
    const params = matchPath(entry.path, pathname);
    if (params) return { handler: entry.handler, params };
  }
  return null;
}

function ensureInterceptor() {
  if (interceptorApplied) return;

  interceptor = new FetchInterceptor();

  interceptor.on('request', async ({ request, controller }) => {
    const url = new URL(request.url);
    const method = request.method.toUpperCase();
    const pathname = url.pathname;

    const match = findHandler(method, pathname);
    if (!match) {
      if (pathname.startsWith('/api/')) {
        console.warn(
          `[msw] Unhandled ${method} ${pathname} — no handler registered. ` +
            `Add server.${method.toLowerCase()}('${pathname.replace('/api/', '')}', handler) in your test.`,
        );
      }
      return;
    }

    const { handler, params } = match;
    const queryParams = Object.fromEntries(url.searchParams);

    let requestBody = '';
    try {
      requestBody = await request.clone().text();
    } catch (_e) {
      // ignore
    }

    const mirageRequest = { params, queryParams, requestBody };
    const result = handler(mirageRequest);

    if (result === undefined || result === null) {
      controller.respondWith(new Response(null, { status: 200 }));
    } else {
      controller.respondWith(
        new Response(JSON.stringify(result), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        }),
      );
    }
  });

  interceptor.apply();
  interceptorApplied = true;
}

function resetHandlers() {
  runtimeHandlers = [];
}

// ─── Server API ──────────────────────────────────────────────────────────────

function normalizePath(path) {
  const cleanPath = path.replace(/^\//, '');
  if (cleanPath.startsWith('api/')) {
    return `/${cleanPath}`;
  }
  return `/api/${cleanPath}`;
}

function createServerCompat() {
  const addHandler = (method, path, handler) => {
    runtimeHandlers.unshift({
      method: method.toUpperCase(),
      path: normalizePath(path),
      handler,
    });
  };

  return {
    get(path, handler) {
      addHandler('GET', path, handler);
    },
    post(path, handler) {
      addHandler('POST', path, handler);
    },
    put(path, handler) {
      addHandler('PUT', path, handler);
    },
    patch(path, handler) {
      addHandler('PATCH', path, handler);
    },
    delete(path, handler) {
      addHandler('DELETE', path, handler);
    },
  };
}

// ─── Test setup ──────────────────────────────────────────────────────────────

export function setupMSW(hooks) {
  hooks.beforeEach(function () {
    ensureInterceptor();
    resetHandlers();
    window.server = createServerCompat();
  });

  hooks.afterEach(function () {
    resetHandlers();
    delete window.server;
  });
}
