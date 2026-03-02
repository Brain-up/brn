/**
 * Lightweight fetch mock for tests.
 *
 * Replaces window.fetch with a handler-based interceptor (no Service Worker).
 * Provides a mirage-compatible `server.get/post/put/delete` API on `window.server`.
 */

// ─── Path matching ────────────────────────────────────────────────────────────

/**
 * Convert a path pattern like '/api/groups/:id' into a regex + param names.
 */
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

// ─── Handler registry ─────────────────────────────────────────────────────────

let runtimeHandlers = []; // added via server.get/post etc. (higher priority)

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

// ─── Fetch interceptor ───────────────────────────────────────────────────────

const originalFetch = window.fetch;
let interceptorInstalled = false;

function findHandler(method, pathname) {
  // Runtime handlers take priority (checked first → last, like worker.use prepend)
  for (const entry of runtimeHandlers) {
    if (entry.method !== method) continue;
    const params = matchPath(entry.path, pathname);
    if (params) return { handler: entry.handler, params };
  }
  // Then default handlers
  for (const entry of defaultHandlers) {
    if (entry.method !== method) continue;
    const params = matchPath(entry.path, pathname);
    if (params) return { handler: entry.handler, params };
  }
  return null;
}

function installInterceptor() {
  if (interceptorInstalled) return;
  interceptorInstalled = true;

  window.fetch = async function (input, init) {
    const request =
      input instanceof Request ? input : new Request(input, init);
    const url = new URL(request.url);
    const method = (init?.method || request.method || 'GET').toUpperCase();
    const pathname = url.pathname;

    const match = findHandler(method, pathname);
    if (!match) {
      // No handler → pass through to real fetch
      return originalFetch.call(window, input, init);
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

    // Build a real Response object
    if (result === undefined || result === null) {
      return new Response(null, { status: 200 });
    }
    return new Response(JSON.stringify(result), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    });
  };
}

function resetHandlers() {
  runtimeHandlers = [];
}

// eslint-disable-next-line no-unused-vars
function restoreFetch() {
  if (interceptorInstalled) {
    window.fetch = originalFetch;
    interceptorInstalled = false;
  }
}

// ─── Mirage-compatible server API ─────────────────────────────────────────────

function normalizePath(path) {
  const cleanPath = path.replace(/^\//, '');
  return `/api/${cleanPath}`;
}

function createServerCompat() {
  const addHandler = (method, path, handler) => {
    // Prepend so later registrations take priority (like worker.use)
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

// ─── Test setup ───────────────────────────────────────────────────────────────

export function setupMSW(hooks) {
  hooks.beforeEach(function () {
    installInterceptor();
    window.server = createServerCompat();
  });

  hooks.afterEach(function () {
    resetHandlers();
  });
}
