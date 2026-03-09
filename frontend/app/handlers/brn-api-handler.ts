import { pluralize } from 'ember-inflector';
import AnswerOption from 'brn/utils/answer-option';
import { ALL_SCHEMAS } from 'brn/schemas';
import type { Handler, NextFn } from '@warp-drive/core/request';
import type { RequestContext, StructuredDataDocument } from '@warp-drive/core/types/request';
import type { Store } from '@warp-drive/core';

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

interface DateTimeLike {
  toUTC(): { toFormat(fmt: string): string };
}

function isDateTimeLike(value: unknown): value is DateTimeLike {
  return typeof value === 'object' && value !== null && typeof (value as DateTimeLike).toUTC === 'function';
}

function buildQueryString(_type: string, query: Record<string, unknown>): string {
  const params: Record<string, string> = {};
  for (const [key, value] of Object.entries(query)) {
    if (value != null) {
      // Statistics adapters transform DateTime to ISO strings
      if (isDateTimeLike(value)) {
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
  'task/environmental-sounds': { serialNumber: 'order' },
  'task/phoneme-pairs': { serialNumber: 'order' },
  'task/auditory-sequence': { serialNumber: 'order' },
  'task/prosody': { serialNumber: 'order' },
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
  ENVIRONMENTAL_SOUNDS: 'task/environmental-sounds',
  PHONEME_PAIRS: 'task/phoneme-pairs',
  AUDITORY_SEQUENCE: 'task/auditory-sequence',
  PROSODY: 'task/prosody',
};

// Derive RELATIONSHIPS, EXCLUDE_FROM_ATTRS, and BELONGS_TO_ID_MAP from the schema definitions.
// This eliminates the need to maintain two parallel sources of truth.
type RelInfo = { type: string; kind: 'belongsTo' | 'hasMany' };
const RELATIONSHIPS: Record<string, Record<string, RelInfo>> = {};
const EXCLUDE_FROM_ATTRS: Record<string, Set<string>> = {};
// Maps "{relName}Id" keys in API payloads to their belongsTo relationship name.
// e.g. for exercise: { seriesId: 'series' } — so `seriesId: 1` becomes `series: { data: { id: "1", type: "series" } }`
const BELONGS_TO_ID_MAP: Record<string, Record<string, string>> = {};

for (const schema of ALL_SCHEMAS) {
  const rels: Record<string, RelInfo> = {};
  const excludes = new Set<string>();
  const idMap: Record<string, string> = {};
  for (const field of schema.fields) {
    if (field.kind === 'belongsTo' || field.kind === 'hasMany') {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      rels[field.name] = { type: field.type!, kind: field.kind };
      excludes.add(field.name);
      // For belongsTo, also exclude and map the "{name}Id" key
      if (field.kind === 'belongsTo') {
        const idKey = `${field.name}Id`;
        excludes.add(idKey);
        idMap[idKey] = field.name;
      }
    }
  }
  if (Object.keys(rels).length > 0) {
    RELATIONSHIPS[schema.type] = rels;
    EXCLUDE_FROM_ATTRS[schema.type] = excludes;
  }
  if (Object.keys(idMap).length > 0) {
    BELONGS_TO_ID_MAP[schema.type] = idMap;
  }
}
// series API sends 'subgroups' (lowercase) which maps to 'subGroups' (camelCase)
EXCLUDE_FROM_ATTRS['series']?.add('subgroups');

interface JsonApiResource {
  id: string;
  type: string;
  attributes: Record<string, unknown>;
  relationships?: Record<string, unknown>;
}

interface JsonApiDocument {
  data: JsonApiResource | JsonApiResource[];
  included?: JsonApiResource[];
}

function normalizeContributor(raw: Record<string, unknown>): JsonApiResource {
  const {
    id, name, nameEn, description, descriptionEn,
    company, companyEn, pictureUrl, contribution,
    active, type, contacts, gitHubLogin, repositoryName,
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
      repositoryName,
    },
  };
}

function normalizeSignalRecord(raw: Record<string, unknown>): JsonApiResource {
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
  allSignals: Record<string, unknown>[],
  store: Store,
): JsonApiResource {
  const id = `signal-task-${signalPayload.id}`;
  // Create answer options with lazy getters that resolve Signal model instances
  // at access time (after CacheHandler has processed the included signals).
  // The JSON:API cache stores attribute values by reference, so these getters survive.
  const opts = allSignals.map((_el, i: number) => {
    const signalId = String(_el.id);
    // Store plain serializable data in attributes to avoid circular references
    // when the JSON:API cache validates via JSON.stringify.
    // signal/audioFileUrl are non-enumerable so JSON.stringify skips them,
    // but they're accessible at runtime for component logic.
    const signalData = _el as Record<string, unknown>;
    const opt = {
      word: `${i + 1}: [${signalData.duration ?? signalData.length}ms, ${signalData.frequency}Mhz]`,
      signalId,
    };
    Object.defineProperties(opt, {
      signal: {
        get() { return store.peekRecord('signal', signalId); },
        enumerable: false,
        configurable: true,
      },
      audioFileUrl: {
        get() { return store.peekRecord('signal', signalId); },
        enumerable: false,
        configurable: true,
      },
    });
    return opt;
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

function normalizeTask(raw: Record<string, unknown>, exerciseId?: string): JsonApiResource {
  const type = MECHANISM_TO_TYPE[raw.exerciseMechanism as string] || 'task/signal';
  const remap = ATTR_REMAP[type] || {};
  const excludes = EXCLUDE_FROM_ATTRS[type] || new Set();

  // Normalize answerOptions
  let opts: unknown[] = [];
  if (raw.answerOptions) {
    if (!Array.isArray(raw.answerOptions)) {
      const answerOptionsObj = raw.answerOptions as Record<string, unknown>;
      Object.keys(answerOptionsObj).forEach((key) => {
        if (Array.isArray(answerOptionsObj[key])) {
          opts = [...opts, ...(answerOptionsObj[key] as unknown[])];
        }
      });
    } else if (raw.correctAnswer) {
      opts = [...(raw.answerOptions as unknown[]), raw.correctAnswer];
    } else {
      opts = [...(raw.answerOptions as unknown[])];
    }
  }
  const normalizedAnswerOptions = opts.map((el) => new AnswerOption(el as ConstructorParameters<typeof AnswerOption>[0]));

  const attributes: Record<string, unknown> = { normalizedAnswerOptions };
  for (const [key, value] of Object.entries(raw)) {
    if (key === 'id' || key === 'type' || excludes.has(key)) continue;
    const attrName = remap[key] || key;
    attributes[attrName] = value;
  }

  // words-sequences (MATRIX) tasks need answerOptions grouped by wordType
  if (type === 'task/words-sequences') {
    if (!attributes.wrongAnswers) {
      attributes.wrongAnswers = [];
    }
    // If answerOptions is a flat array, group by wordType for the MATRIX component
    if (Array.isArray(attributes.answerOptions)) {
      const grouped: Record<string, unknown[]> = {};
      for (const opt of attributes.answerOptions as { wordType?: string }[]) {
        const wordType = opt.wordType || 'unknown';
        if (!grouped[wordType]) {
          grouped[wordType] = [];
        }
        grouped[wordType].push(opt);
      }
      attributes.answerOptions = grouped;
    }
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

function normalizeRecord(modelType: string, raw: Record<string, unknown>, included: JsonApiResource[], store: Store): JsonApiResource {
  if (modelType === 'contributor') {
    return normalizeContributor(raw);
  }

  const pk = PRIMARY_KEY[modelType] || 'id';
  const id = String(raw[pk]);
  const remap = ATTR_REMAP[modelType] || {};
  const aliases = ATTR_ALIAS[modelType] || {};
  const rels = RELATIONSHIPS[modelType] || {};
  const excludes = EXCLUDE_FROM_ATTRS[modelType] || new Set();

  // Series: remap 'type' → 'kind', 'subgroups' → 'subGroups'
  if (modelType === 'series') {
    raw.kind = raw.type;
    // API sends 'subgroups' (lowercase) but schema uses 'subGroups' (camelCase)
    if ('subgroups' in raw && !('subGroups' in raw)) {
      raw.subGroups = raw.subgroups;
      delete raw.subgroups;
    }
  }

  // Signal: remap 'length' → 'duration'
  if (modelType === 'signal') {
    raw.duration = raw.length;
  }

  const attributes: Record<string, unknown> = {};
  const relationships: Record<string, unknown> = {};

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
      for (const taskData of value as Record<string, unknown>[]) {
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
          data: (value as unknown[]).map((item: unknown) => ({
            id: String(typeof item === 'object' && item !== null ? (item as Record<string, unknown>).id : item),
            type: rel.type,
          })),
        };
      } else if (rel.kind === 'belongsTo') {
        if (value != null) {
          relationships[key] = {
            data: { id: String(typeof value === 'object' ? (value as Record<string, unknown>).id : value), type: rel.type },
          };
        } else {
          relationships[key] = { data: null };
        }
      }
      continue;
    }

    if (excludes.has(key)) {
      // Check if this is a "{relName}Id" key that should become a belongsTo relationship
      const idMap = BELONGS_TO_ID_MAP[modelType];
      if (idMap && key in idMap) {
        const relName = idMap[key];
        const relInfo = rels[relName];
        if (relInfo && relInfo.kind === 'belongsTo' && value != null && !relationships[relName]) {
          relationships[relName] = {
            data: { id: String(value), type: relInfo.type },
          };
        }
      }
      continue;
    }

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

// ─── Op Resolution ───────────────────────────────────────────────────────────

interface FindRecordData {
  record: { type: string; id: string };
  options?: Record<string, unknown>;
}

interface QueryData {
  type: string;
  query: Record<string, unknown>;
  options?: Record<string, unknown>;
}

interface FindAllData {
  type: string;
  options?: Record<string, unknown>;
}

type OpData = FindRecordData | QueryData | FindAllData;
type OpResolver = (data: OpData) => { type: string; url: string };

const OP_RESOLVERS: Record<string, OpResolver> = {
  findRecord(data) {
    const { record } = data as FindRecordData;
    return { type: record.type, url: `/api/${buildPath(record.type, record.id)}` };
  },
  query(data) {
    const { type, query } = data as QueryData;
    return { type, url: `/api/${buildPath(type)}${buildQueryString(type, query)}` };
  },
  findAll(data) {
    const { type } = data as FindAllData;
    return { type, url: `/api/${buildPath(type)}` };
  },
};

function resolveOp(op: string, data: OpData): { type: string; url: string } | null {
  const resolver = OP_RESOLVERS[op];
  return resolver ? resolver(data) : null;
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
export class BrnApiHandler implements Handler {
  private store: Store;

  constructor(store: Store) {
    this.store = store;
  }

  async request<T>(context: RequestContext, next: NextFn<T>): Promise<T | StructuredDataDocument<T>> {
    const { op, data } = context.request as { op?: string; data?: OpData } & Record<string, unknown>;

    // Build URL based on operation type
    const resolved = op && data ? resolveOp(op, data) : null;
    if (!resolved) {
      // Pass through for ops we don't handle (e.g. saveRecord)
      return next(context.request);
    }
    const { type, url } = resolved;

    // Set the URL on the request and fetch
    (context.request as Record<string, unknown>).url = url;
    (context.request as Record<string, unknown>).method = (context.request as Record<string, unknown>).method || 'GET';

    // next() returns a StructuredDocument { request, response, content }
    // where content is the parsed JSON from Fetch
    const result = await next(context.request);
    const json = (result as StructuredDataDocument<Record<string, unknown>>)?.content ?? result;

    if (!json || typeof json !== 'object') {
      return json as T | StructuredDataDocument<T>;
    }

    // Unwrap { data: ... } envelope from the API response
    const jsonObj = json as Record<string, unknown>;
    const rawData = jsonObj.data ?? jsonObj;

    // Normalize to JSON:API
    const included: JsonApiResource[] = [];
    let normalized: JsonApiDocument;

    if (Array.isArray(rawData)) {
      const resources = rawData.map((item) => normalizeRecord(type, item as Record<string, unknown>, included, this.store));
      normalized = { data: resources };
    } else if (rawData && typeof rawData === 'object') {
      const resource = normalizeRecord(type, rawData as Record<string, unknown>, included, this.store);
      normalized = { data: resource };
    } else {
      return json as T | StructuredDataDocument<T>;
    }

    if (included.length > 0) {
      normalized.included = included;
    }

    return normalized as unknown as T;
  }
}
