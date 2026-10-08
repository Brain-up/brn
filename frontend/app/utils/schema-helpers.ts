import { storeFor } from '@warp-drive/core';
import type { OpaqueRecordInstance } from '@warp-drive/core/types/record';
import { getOwner } from '@ember/application';

/**
 * Look up an Ember service from a SchemaRecord instance.
 *
 * SchemaRecords don't support DI, so we go through the store's owner
 * to resolve services.
 */
export function getService<T>(record: unknown, serviceName: string): T | null {
  const store = storeFor(record as OpaqueRecordInstance, true);
  if (!store) return null;
  const owner = getOwner(store);
  if (!owner) return null;
  return owner.lookup(`service:${serviceName}`) as T;
}
