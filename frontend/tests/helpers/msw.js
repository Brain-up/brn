import { setupWorker } from 'msw/browser';
import { http, HttpResponse } from 'msw';

// Default handlers matching the mirage config
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
    new HttpResponse(null, { status: 200 }),
  ),
  http.get('/api/v2/statistics/study/year', () =>
    new HttpResponse(null, { status: 200 }),
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

let worker;
let workerStarted = false;

function normalizePath(path) {
  const cleanPath = path.replace(/^\//, '');
  return `/api/${cleanPath}`;
}

function createMirageCompatHandler(method, path, handler) {
  const mswPath = normalizePath(path);
  const httpMethod = http[method];

  return httpMethod(mswPath, async ({ request, params }) => {
    const url = new URL(request.url);
    const queryParams = Object.fromEntries(url.searchParams);
    let requestBody = '';
    try {
      requestBody = await request.text();
    } catch (_e) {
      // ignore
    }

    const mirageRequest = { params, queryParams, requestBody };
    const result = handler(null, mirageRequest);

    if (result === undefined || result === null) {
      return new HttpResponse(null, { status: 200 });
    }
    return HttpResponse.json(result);
  });
}

function createServerCompat() {
  return {
    get(path, handler) {
      worker.use(createMirageCompatHandler('get', path, handler));
    },
    post(path, handler) {
      worker.use(createMirageCompatHandler('post', path, handler));
    },
    put(path, handler) {
      worker.use(createMirageCompatHandler('put', path, handler));
    },
    delete(path, handler) {
      worker.use(createMirageCompatHandler('delete', path, handler));
    },
  };
}

async function startMSW() {
  if (!workerStarted) {
    worker = setupWorker(...defaultHandlers);
    await worker.start({
      onUnhandledRequest: 'bypass',
      quiet: true,
    });
    workerStarted = true;
    window.server = createServerCompat();
  }
}

export function setupMSW(hooks) {
  hooks.beforeEach(async function () {
    await startMSW();
  });

  hooks.afterEach(function () {
    worker.resetHandlers();
  });
}
