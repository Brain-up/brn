import { useLegacyStore } from '@warp-drive/legacy';
import { JSONAPICache } from '@warp-drive/json-api';
import { RequestManager, Fetch, CacheHandler } from '@warp-drive/core';
import { EnableHydration } from '@warp-drive/core/types/request';
import { inject as service } from '@ember/service';
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

export default class Store extends LegacyStore {
  @service('auth-token') declare authToken: AuthTokenService;

  constructor(args: any) {
    super(args);
    this.requestManager = new RequestManager()
      .use([new AuthHandler(this.authToken), new BrnApiHandler(this), Fetch])
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
    const promise = this.request({
      op: 'findRecord',
      data: { record: identifier, options: options || {} },
      [EnableHydration]: true,
    });
    // CacheHandler returns a reactive document { data: record }; unwrap to get the model instance
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }

  // @ts-expect-error - return type differs from base (we unwrap the reactive document)
  query<T>(type: string, query: Record<string, unknown>, options?: Record<string, unknown>): Promise<T[]> {
    const promise = this.request({
      op: 'query',
      data: { type, query, options: options || {} },
      [EnableHydration]: true,
    });
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }

  // @ts-expect-error - return type differs from base (we unwrap the reactive document)
  findAll<T>(type: string, options?: Record<string, unknown>): Promise<T[]> {
    const promise = this.request({
      op: 'findAll',
      data: { type, options: options || {} },
      [EnableHydration]: true,
    });
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }
}

declare module '@ember/service' {
  interface Registry {
    store: Store;
  }
}
