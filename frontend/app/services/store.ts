import { useLegacyStore } from '@warp-drive-mirror/legacy';
import { JSONAPICache } from '@warp-drive-mirror/json-api';
import { RequestManager, Fetch, CacheHandler } from '@warp-drive-mirror/core';
import { EnableHydration } from '@warp-drive-mirror/core/types/request';
import { inject as service } from '@ember/service';
import { AuthHandler } from 'brn/handlers/auth-handler';
import { BrnApiHandler } from 'brn/handlers/brn-api-handler';
import type AuthTokenService from 'brn/services/auth-token';

const LegacyStore = useLegacyStore({
  linksMode: false,
  legacyRequests: true,
  cache: JSONAPICache,
});

export default class Store extends LegacyStore {
  @service('auth-token') declare authToken: AuthTokenService;

  constructor(args: any) {
    super(args);
    this.requestManager = new RequestManager()
      .use([new AuthHandler(this.authToken), new BrnApiHandler(), Fetch])
      .useCache(CacheHandler);
  }

  // Override legacy store methods to NOT set SkipCache and to enable hydration.
  // The legacy versions set SkipCache because LegacyNetworkHandler handles caching
  // internally. Our custom BrnApiHandler returns JSON:API documents that need to
  // go through the CacheHandler for proper caching and record hydration.

  findRecord(type: string, id: string | number, options: any = {}): any {
    const normalizedId = String(id);
    const identifier = this.cacheKeyManager.getOrCreateRecordIdentifier({ type, id: normalizedId });
    const promise = this.request({
      op: 'findRecord',
      data: { record: identifier, options },
      [EnableHydration]: true,
    });
    // CacheHandler returns a reactive document { data: record }; unwrap to get the model instance
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }

  query(type: string, query: any, options: any = {}): any {
    const promise = this.request({
      op: 'query',
      data: { type, query, options },
      [EnableHydration]: true,
    });
    // CacheHandler returns a reactive document { data: records[] }
    // The old RecordArray was iterable; the reactive document is not.
    // Return the data array for template compatibility.
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }

  findAll(type: string, options: any = {}): any {
    const promise = this.request({
      op: 'findAll',
      data: { type, options },
      [EnableHydration]: true,
    });
    return promise.then((doc: any) => doc.content?.data ?? doc.content);
  }
}
