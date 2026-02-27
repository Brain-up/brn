import { pluralize } from 'ember-inflector';
import AnswerOption from 'brn/utils/answer-option';

// ─── URL Building ────────────────────────────────────────────────────────────

const PATH_OVERRIDES: Record<string, string> = {
  headphone: 'users/current/headphones',
  'user-weekly-statistics': 'v2/statistics/study/week',
  'user-yearly-statistics': 'v2/statistics/study/year',
  'user-daily-time-table-statistics': 'v2/statistics/study/day',
};

function buildPath(type: string, id?: string): string {
  const override = PATH_OVERRIDES[type];
  if (override) {
    return id ? `${override}/${id}` : override;
  }
  // task/* subtypes all use /tasks
  let pathType = type;
  if (type.startsWith('task/')) {
    pathType = 'task';
  }
  const path = pluralize(pathType);
  return id ? `${path}/${id}` : path;
}

function buildQueryString(type: string, query: Record<string, any>): string {
  const params: Record<string, string> = {};
  for (const [key, value] of Object.entries(query)) {
    if (value != null) {
      // Statistics adapters transform DateTime to ISO strings
      if (value && typeof value === 'object' && typeof value.toUTC === 'function') {
        params[key] = value.toUTC().toFormat("yyyy-MM-dd'T'HH:mm:ss");
      } else {
        params[key] = String(value);
      }
    }
  }
  const qs = new URLSearchParams(params).toString();
  return qs ? `?${qs}` : '';
}

// ─── JSON:API Normalization ──────────────────────────────────────────────────

// Maps raw API key → JSON:API attribute name
const ATTR_REMAP: Record<string, Record<string, string>> = {
  task: { serialNumber: 'order' },
  'task/signal': { serialNumber: 'order' },
  'task/single-simple-words': { serialNumber: 'order' },
  'task/words-sequences': { serialNumber: 'order' },
};

// Model attrs that should be aliased from another raw key.
// { modelAttr: rawKey } — after normal attribute processing, copy rawKey's value to modelAttr.
// This handles the case where the old keyForAttribute mapped multiple model attrs to the same JSON key.
const ATTR_ALIAS: Record<string, Record<string, string>> = {
  exercise: { order: 'level' }, // model has both @attr level and @attr order; API sends only 'level'
};

const PRIMARY_KEY: Record<string, string> = {
  'user-weekly-statistics': 'date',
  'user-yearly-statistics': 'date',
  'user-daily-time-table-statistics': 'seriesName',
};

const MECHANISM_TO_TYPE: Record<string, string> = {
  MATRIX: 'task/words-sequences',
  SIGNALS: 'task/signal',
  WORDS: 'task/single-simple-words',
};

// Attributes that are actually relationships
const RELATIONSHIPS: Record<string, Record<string, { type: string; kind: 'belongsTo' | 'hasMany' }>> = {
  group: {
    series: { type: 'series', kind: 'hasMany' },
  },
  series: {
    group: { type: 'group', kind: 'belongsTo' },
    subGroups: { type: 'subgroup', kind: 'hasMany' },
    exercises: { type: 'exercise', kind: 'hasMany' },
  },
  subgroup: {
    exercises: { type: 'exercise', kind: 'hasMany' },
  },
  exercise: {
    series: { type: 'series', kind: 'belongsTo' },
    parent: { type: 'subgroup', kind: 'belongsTo' },
    tasks: { type: 'task', kind: 'hasMany' },
    signals: { type: 'signal', kind: 'hasMany' },
  },
  task: {
    exercise: { type: 'exercise', kind: 'belongsTo' },
  },
  'task/signal': {
    exercise: { type: 'exercise', kind: 'belongsTo' },
    signal: { type: 'signal', kind: 'belongsTo' },
  },
  'task/single-simple-words': {
    exercise: { type: 'exercise', kind: 'belongsTo' },
  },
  'task/words-sequences': {
    exercise: { type: 'exercise', kind: 'belongsTo' },
  },
};

// Fields to exclude from attributes (they're relationships or metadata)
const EXCLUDE_FROM_ATTRS: Record<string, Set<string>> = {
  group: new Set(['series']),
  series: new Set(['group', 'subGroups', 'exercises']),
  subgroup: new Set(['exercises']),
  exercise: new Set(['series', 'parent', 'tasks', 'signals']),
  task: new Set(['exercise']),
  'task/signal': new Set(['exercise', 'signal']),
  'task/single-simple-words': new Set(['exercise']),
  'task/words-sequences': new Set(['exercise']),
};

interface JsonApiResource {
  id: string;
  type: string;
  attributes: Record<string, any>;
  relationships?: Record<string, any>;
}

interface JsonApiDocument {
  data: JsonApiResource | JsonApiResource[];
  included?: JsonApiResource[];
}

function normalizeContributor(raw: any): JsonApiResource {
  const {
    id, name, nameEn, description, descriptionEn,
    company, companyEn, pictureUrl, contribution,
    active, type, contacts, gitHubLogin,
  } = raw;
  return {
    id: String(id),
    type: 'contributor',
    attributes: {
      rawName: { 'ru-ru': name ?? '', 'en-us': nameEn ?? name ?? '' },
      rawDescription: { 'ru-ru': description ?? '', 'en-us': descriptionEn ?? description ?? '' },
      rawCompany: { 'ru-ru': company ?? '', 'en-us': companyEn ?? company ?? '' },
      avatar: pictureUrl,
      login: gitHubLogin,
      contribution,
      isActive: active,
      kind: type,
      contacts,
    },
  };
}

function normalizeSignalRecord(raw: any): JsonApiResource {
  const { id, ...rest } = raw;
  return {
    id: String(id),
    type: 'signal',
    attributes: {
      ...rest,
      duration: raw.length,
    },
  };
}

function normalizeTaskSignalRecord(
  signalPayload: { id: number },
  exerciseId: string,
  allSignals: any[],
  store: any,
): JsonApiResource {
  const id = `signal-task-${signalPayload.id}`;
  // Create answer options with lazy getters that resolve Signal model instances
  // at access time (after CacheHandler has processed the included signals).
  // The JSON:API cache stores attribute values by reference, so these getters survive.
  const opts = allSignals.map((_el: any, i: number) => {
    const signalId = String(_el.id);
    return {
      get word() {
        const sig = store.peekRecord('signal', signalId);
        return `${i + 1}: [${sig?.duration}ms, ${sig?.frequency}Mhz]`;
      },
      get signal() {
        return store.peekRecord('signal', signalId);
      },
      get audioFileUrl(): any {
        return this.signal;
      },
    };
  });
  return {
    id,
    type: 'task/signal',
    attributes: {
      exerciseMechanism: 'SIGNALS',
      answerOptions: opts,
      normalizedAnswerOptions: opts,
    },
    relationships: {
      signal: { data: { id: String(signalPayload.id), type: 'signal' } },
      exercise: { data: { id: exerciseId, type: 'exercise' } },
    },
  };
}

function normalizeTask(raw: any, exerciseId?: string): JsonApiResource {
  const type = MECHANISM_TO_TYPE[raw.exerciseMechanism] || 'task/signal';
  const remap = ATTR_REMAP[type] || {};
  const excludes = EXCLUDE_FROM_ATTRS[type] || new Set();

  // Normalize answerOptions
  let opts: any[] = [];
  if (raw.answerOptions) {
    if (!Array.isArray(raw.answerOptions)) {
      Object.keys(raw.answerOptions).forEach((key) => {
        if (Array.isArray(raw.answerOptions[key])) {
          opts = [...opts, ...raw.answerOptions[key]];
        }
      });
    } else if (raw.correctAnswer) {
      opts = [...raw.answerOptions, raw.correctAnswer];
    } else {
      opts = [...raw.answerOptions];
    }
  }
  const normalizedAnswerOptions = opts.map((el: any) => new AnswerOption(el));

  const attributes: Record<string, any> = { normalizedAnswerOptions };
  for (const [key, value] of Object.entries(raw)) {
    if (key === 'id' || key === 'type' || excludes.has(key)) continue;
    const attrName = remap[key] || key;
    attributes[attrName] = value;
  }

  // words-sequences always init wrongAnswers
  if (type === 'task/words-sequences' && !attributes.wrongAnswers) {
    attributes.wrongAnswers = [];
  }

  const resource: JsonApiResource = {
    id: String(raw.id),
    type,
    attributes,
  };

  if (exerciseId) {
    resource.relationships = {
      exercise: { data: { id: exerciseId, type: 'exercise' } },
    };
  }

  return resource;
}

function normalizeRecord(modelType: string, raw: any, included: JsonApiResource[], store: any): JsonApiResource {
  if (modelType === 'contributor') {
    return normalizeContributor(raw);
  }

  const pk = PRIMARY_KEY[modelType] || 'id';
  const id = String(raw[pk]);
  const remap = ATTR_REMAP[modelType] || {};
  const aliases = ATTR_ALIAS[modelType] || {};
  const rels = RELATIONSHIPS[modelType] || {};
  const excludes = EXCLUDE_FROM_ATTRS[modelType] || new Set();

  // Series: remap 'type' → 'kind'
  if (modelType === 'series') {
    raw.kind = raw.type;
  }

  // Signal: remap 'length' → 'duration'
  if (modelType === 'signal') {
    raw.duration = raw.length;
  }

  const attributes: Record<string, any> = {};
  const relationships: Record<string, any> = {};

  // For exercises, process signals BEFORE tasks so that signal-based task/signal
  // records are created first. If signals exist, they define the tasks relationship.
  if (modelType === 'exercise' && Array.isArray(raw.signals) && raw.signals.length > 0) {
    const signalRefs: { id: string; type: string }[] = [];
    const taskRefs: { id: string; type: string }[] = [];
    for (const signalData of raw.signals) {
      const signalResource = normalizeSignalRecord(signalData);
      included.push(signalResource);
      signalRefs.push({ id: signalResource.id, type: 'signal' });

      const taskSignalResource = normalizeTaskSignalRecord(signalData, id, raw.signals, store);
      included.push(taskSignalResource);
      taskRefs.push({ id: taskSignalResource.id, type: 'task/signal' });
    }
    relationships.signals = { data: signalRefs };
    // For signal exercises, tasks ARE the task/signal records
    relationships.tasks = { data: taskRefs };
  }

  for (const [key, value] of Object.entries(raw)) {
    // Don't skip pk from attributes when it's also a model attribute (stats models)
    if (key === 'id') continue;
    if (key === pk && pk !== 'id') {
      // Include the pk value as an attribute too (e.g. 'date', 'seriesName')
      attributes[key] = value;
      continue;
    }

    // Handle exercise tasks specially — embed full task records
    if (modelType === 'exercise' && key === 'tasks' && Array.isArray(value)) {
      // If signals already set the tasks relationship, skip regular tasks
      if (relationships.tasks) continue;
      const taskRefs: { id: string; type: string }[] = [];
      for (const taskData of value as any[]) {
        const taskResource = normalizeTask(taskData, id);
        included.push(taskResource);
        taskRefs.push({ id: taskResource.id, type: taskResource.type });
      }
      relationships.tasks = { data: taskRefs };
      continue;
    }

    // Skip signals — already handled above for exercises
    if (modelType === 'exercise' && key === 'signals') {
      continue;
    }

    // Check if this is a relationship field
    const rel = rels[key];
    if (rel) {
      if (rel.kind === 'hasMany' && Array.isArray(value)) {
        relationships[key] = {
          data: (value as any[]).map((item: any) => ({
            id: String(typeof item === 'object' ? item.id : item),
            type: rel.type,
          })),
        };
      } else if (rel.kind === 'belongsTo') {
        if (value != null) {
          relationships[key] = {
            data: { id: String(typeof value === 'object' ? value.id : value), type: rel.type },
          };
        } else {
          relationships[key] = { data: null };
        }
      }
      continue;
    }

    if (excludes.has(key)) continue;

    // Regular attribute — apply remapping
    const attrName = remap[key] || key;
    attributes[attrName] = value;
  }

  // Apply attribute aliases (copy one attr's value to another attr name)
  for (const [attrName, sourceKey] of Object.entries(aliases)) {
    if (sourceKey in attributes) {
      attributes[attrName] = attributes[sourceKey];
    }
  }

  const resource: JsonApiResource = { id, type: modelType, attributes };
  if (Object.keys(relationships).length > 0) {
    resource.relationships = relationships;
  }
  return resource;
}

// ─── Handler ─────────────────────────────────────────────────────────────────

/**
 * Request handler that:
 * 1. Builds URLs from legacy store ops (findRecord, query, findAll)
 * 2. Makes the fetch request
 * 3. Normalizes REST responses to JSON:API format for the cache
 *
 * Replaces all adapters and serializers.
 */
export class BrnApiHandler {
  private store: any;

  constructor(store: any) {
    this.store = store;
  }

  async request<T>(context: any, next: (req: any) => Promise<T>): Promise<T> {
    const { op, data } = context.request;

    // Build URL based on operation type
    let url: string;
    let type: string;

    if (op === 'findRecord') {
      type = data.record.type;
      url = `/api/${buildPath(type, data.record.id)}`;
    } else if (op === 'query') {
      type = data.type;
      url = `/api/${buildPath(type)}${buildQueryString(type, data.query)}`;
    } else if (op === 'findAll') {
      type = data.type;
      url = `/api/${buildPath(type)}`;
    } else {
      // Pass through for ops we don't handle (e.g. saveRecord)
      return next(context.request);
    }

    // Set the URL on the request and fetch
    context.request.url = url;
    context.request.method = context.request.method || 'GET';

    // next() returns a StructuredDocument { request, response, content }
    // where content is the parsed JSON from Fetch
    const result: any = await next(context.request);
    const json: any = result?.content ?? result;

    if (!json || typeof json !== 'object') {
      return json;
    }

    // Unwrap { data: ... } envelope from the API response
    const rawData = json.data ?? json;

    // Normalize to JSON:API
    const included: JsonApiResource[] = [];
    let normalized: JsonApiDocument;

    if (Array.isArray(rawData)) {
      const resources = rawData.map((item: any) => normalizeRecord(type, item, included, this.store));
      normalized = { data: resources };
    } else if (rawData && typeof rawData === 'object') {
      const resource = normalizeRecord(type, rawData, included, this.store);
      normalized = { data: resource };
    } else {
      return json;
    }

    if (included.length > 0) {
      normalized.included = included;
    }

    return normalized as T;
  }
}
