/**
 * New-style Transformation for full-date fields.
 *
 * Used with `kind: 'field'` schemas to convert between raw date strings
 * in the cache and Luxon DateTime objects on the record.
 *
 * hydrate: raw string -> DateTime (used on get)
 * serialize: DateTime -> raw string (used on set)
 */
import { Type } from '@warp-drive/core/types/symbols';
import type { Transformation } from '@warp-drive/core/reactive';
import { DateTime } from 'luxon';

export const FullDateTransformation: Transformation<string | null, DateTime | null> = {
  hydrate(
    value: string | null | undefined,
    _options: Record<string, unknown> | null,
  ): DateTime | null {
    if (!value) return null;
    const browserLocale = (typeof navigator !== 'undefined' && navigator.language) || 'en';
    return DateTime.fromISO(value, { zone: 'utc', locale: browserLocale });
  },

  serialize(
    value: DateTime | Date | null,
    _options: Record<string, unknown> | null,
  ): string | null {
    if (!value) return null;
    if (value instanceof Date) {
      return value.toISOString();
    }
    return (value as DateTime).toISO() ?? null;
  },

  [Type]: 'full-date',
};
