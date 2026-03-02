/**
 * MSW test helper with @mswjs/interceptors for reliable fetch interception.
 *
 * Uses @mswjs/interceptors/fetch (same mechanism as MSW's setupServer).
 * App-level @ember/test-waiters integration in services/network.ts ensures
 * settled() waits for all pending requests.
 * Provides a mirage-compatible server.get/post/put/delete API.
 */
import { FetchInterceptor } from '@mswjs/interceptors/fetch';

// ─── Path matching ───────────────────────────────────────────────────────────

function compilePath(pattern) {
  const paramNames = [];
  const regexStr = pattern.replace(/:([^/]+)/g, (_match, name) => {
    paramNames.push(name);
    return '([^/]+)';
  });
  return { regex: new RegExp(`^${regexStr}$`), paramNames };
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
      data: {
        firstName: 'First-Name',
        lastName: 'Last-Name',
        email: 'em@il',
      },
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
  { method: 'GET', path: '/api/groups/:id', handler: () => ({ data: {} }) },
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
  { method: 'GET', path: '/api/tasks/:id', handler: () => ({ data: {} }) },
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
    const result = handler(null, mirageRequest);

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

// ─── Mirage-compatible server API ────────────────────────────────────────────

function normalizePath(path) {
  const cleanPath = path.replace(/^\//, '');
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
  });
}
