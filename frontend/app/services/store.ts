import { useLegacyStore } from '@warp-drive/legacy';
import { JSONAPICache } from '@warp-drive/json-api';
import { RequestManager, Fetch, CacheHandler } from '@warp-drive/core';
import { EnableHydration } from '@warp-drive/core/types/request';
import type { StructuredDataDocument } from '@warp-drive/core/types/request';
import type { Store as WarpDriveStore } from '@warp-drive/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import { AuthHandler } from 'brn/handlers/auth-handler';
import { BrnApiHandler } from 'brn/handlers/brn-api-handler';
import type AuthTokenService from 'brn/services/auth-token';
import { ALL_SCHEMAS, ALL_EXTENSIONS } from 'brn/schemas';
import { FullDateTransformation } from 'brn/transformations/full-date';
import { ArrayTransformation } from 'brn/transformations/array';

const LegacyStore = useLegacyStore({
  linksMode: false,
  legacyRequests: true,
  cache: JSONAPICache,
  schemas: ALL_SCHEMAS,
  transformations: [FullDateTransformation, ArrayTransformation],
  CAUTION_MEGA_DANGER_ZONE_extensions: ALL_EXTENSIONS,
});

interface ReactiveContent {
  data?: unknown;
}

/** Unwrap a single record from a reactive document. */
function unwrapOne(doc: StructuredDataDocument<ReactiveContent>): unknown {
  const content = doc.content;
  return (content as ReactiveContent)?.data ?? content;
}

/** Unwrap an array from a reactive document, converting to native Array. */
function unwrapMany(doc: StructuredDataDocument<ReactiveContent>): unknown[] {
  const content = doc.content;
  const data = (content as ReactiveContent)?.data ?? content;
  return Array.from((data as Iterable<unknown>) ?? []);
}

export default class Store extends LegacyStore {
  @service('auth-token') declare authToken: AuthTokenService;

  constructor(args: Record<string, unknown>) {
    super(args);
    this.requestManager = new RequestManager()
      .use([new AuthHandler(this.authToken), new BrnApiHandler(this as unknown as WarpDriveStore), Fetch])
      .useCache(CacheHandler);
  }

  // Override legacy store methods to NOT set SkipCache and to enable hydration.
  // The legacy versions set SkipCache because LegacyNetworkHandler handles caching
  // internally. Our custom BrnApiHandler returns JSON:API documents that need to
  // go through the CacheHandler for proper caching and record hydration.

  // @ts-expect-error - return type differs from base (we unwrap the reactive document)
  findRecord<T>(type: string, id: string | number, options?: Record<string, unknown>): Promise<T> {
    const normalizedId = String(id);
    const identifier = this.cacheKeyManager.getOrCreateRecordIdentifier({ type, id: normalizedId });
    return this.request({
      op: 'findRecord',
      data: { record: identifier, options: options || {} },
      [EnableHydration]: true,
    }).then(unwrapOne) as Promise<T>;
  }

  // @ts-expect-error - return type differs from base (we unwrap the reactive document)
  query<T>(type: string, query: Record<string, unknown>, options?: Record<string, unknown>): Promise<T[]> {
    return this.request({
      op: 'query',
      data: { type, query, options: options || {} },
      [EnableHydration]: true,
    }).then(unwrapMany) as Promise<T[]>;
  }

  // @ts-expect-error - return type differs from base (we unwrap the reactive document)
  findAll<T>(type: string, options?: Record<string, unknown>): Promise<T[]> {
    return this.request({
      op: 'findAll',
      data: { type, options: options || {} },
      [EnableHydration]: true,
    }).then(unwrapMany) as Promise<T[]>;
  }
}

declare module '@ember/service' {
  interface Registry {
    store: Store;
  }
}
