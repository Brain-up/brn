/**
 * New-style Transformation for array fields.
 *
 * Used with `kind: 'field'` schemas to ensure values are always arrays.
 *
 * hydrate: raw value -> array (used on get)
 * serialize: array -> array (used on set)
 */
import { Type } from '@warp-drive-mirror/core/types/symbols';
import type { Transformation } from '@warp-drive-mirror/core/reactive';
import type { ArrayValue, Value } from '@warp-drive-mirror/core/types/json/raw';

function transformToArray(target: Value | undefined): ArrayValue {
  if (target == null || (Array.isArray(target) && target.length === 0)) {
    return [];
  }
  return Array.isArray(target) ? target : [target as Value];
}

export const ArrayTransformation: Transformation<ArrayValue, ArrayValue> = {
  hydrate(
    value: ArrayValue | undefined,
    _options: Record<string, unknown> | null,
  ): ArrayValue {
    return transformToArray(value);
  },

  serialize(
    value: ArrayValue,
    _options: Record<string, unknown> | null,
  ): ArrayValue {
    return transformToArray(value);
  },

  [Type]: 'array',
};
