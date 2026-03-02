/**
 * MSW v2 test helper with @ember/test-waiters integration.
 *
 * Uses setupWorker (Service Worker) for proper network-level interception.
 * Wraps fetch with a test-waiter so settled() waits for all pending requests.
 * Provides a mirage-compatible server.get/post/put/delete API via worker.use().
 */
import { http, HttpResponse } from 'msw';
import { setupWorker } from 'msw/browser';
import { buildWaiter, waitForPromise } from '@ember/test-waiters';

// ─── Test waiter for fetch tracking ──────────────────────────────────────────

const fetchWaiter = buildWaiter('fetch-waiter');

// ─── Default handlers ────────────────────────────────────────────────────────

const defaultHandlers = [
  http.get('/api/users/current', () =>
    HttpResponse.json({
      data: {
        firstName: 'First-Name',
        lastName: 'Last-Name',
        email: 'em@il',
      },
    }),
  ),
  http.get('/api/v2/statistics/study/week', () =>
    HttpResponse.json({ data: [] }),
  ),
  http.get('/api/v2/statistics/study/year', () =>
    HttpResponse.json({ data: [] }),
  ),
  http.get('/api/groups', () => HttpResponse.json({ data: [] })),
  http.get('/api/groups/:id', () => HttpResponse.json({ data: {} })),
  http.get('/api/subgroups', () => HttpResponse.json({ data: [] })),
  http.get('/api/series', () => HttpResponse.json({ data: [] })),
  http.get('/api/series/:id', () => HttpResponse.json({ data: {} })),
  http.get('/api/exercises', () => HttpResponse.json({ data: [] })),
  http.get('/api/exercises/:id', () => HttpResponse.json({ data: {} })),
  http.get('/api/tasks', () => HttpResponse.json({ data: [] })),
  http.get('/api/tasks/:id', () => HttpResponse.json({ data: {} })),
  http.post('/api/study-history', () => HttpResponse.json({ id: '1' })),
  http.post('/api/exercises/byIds', () => HttpResponse.json({ data: [] })),
];

// ─── Worker singleton ────────────────────────────────────────────────────────

let worker;
let workerStarted = false;

async function ensureWorker() {
  if (!worker) {
    worker = setupWorker(...defaultHandlers);
  }
  if (!workerStarted) {
    await waitForPromise(
      worker.start({ onUnhandledRequest: 'bypass', quiet: true }),
    );
    workerStarted = true;
  }
}

// ─── Fetch waiter wrapper ────────────────────────────────────────────────────

let fetchWrapped = false;
let nativeFetch;

function wrapFetchWithWaiter() {
  if (fetchWrapped) return;
  fetchWrapped = true;
  nativeFetch = window.fetch;

  window.fetch = function (...args) {
    const token = fetchWaiter.beginAsync();
    return nativeFetch
      .apply(window, args)
      .then((response) => {
        fetchWaiter.endAsync(token);
        return response;
      })
      .catch((error) => {
        fetchWaiter.endAsync(token);
        throw error;
      });
  };
}

// ─── Mirage-compatible server API ────────────────────────────────────────────

function normalizePath(path) {
  const cleanPath = path.replace(/^\//, '');
  return `/api/${cleanPath}`;
}

function createMSWHandler(method, path, mirageHandler) {
  const fullPath = normalizePath(path);
  const httpMethod = http[method.toLowerCase()];

  return httpMethod(fullPath, async ({ request, params }) => {
    const url = new URL(request.url);
    const queryParams = Object.fromEntries(url.searchParams);

    let requestBody = '';
    try {
      requestBody = await request.clone().text();
    } catch (_e) {
      // ignore
    }

    const mirageRequest = { params, queryParams, requestBody };
    const result = mirageHandler(null, mirageRequest);

    if (result === undefined || result === null) {
      return new HttpResponse(null, { status: 200 });
    }
    return HttpResponse.json(result);
  });
}

function createServerCompat() {
  const addHandler = (method, path, handler) => {
    worker.use(createMSWHandler(method, path, handler));
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
  hooks.beforeEach(async function () {
    await ensureWorker();
    wrapFetchWithWaiter();
    worker.resetHandlers(...defaultHandlers);
    window.server = createServerCompat();
  });

  hooks.afterEach(function () {
    worker.resetHandlers(...defaultHandlers);
  });
}
