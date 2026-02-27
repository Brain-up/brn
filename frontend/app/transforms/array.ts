import { Transform } from '@warp-drive-mirror/legacy/serializer/transform';

function transformToArray(target: unknown): unknown[] {
  if (target == null || (Array.isArray(target) && target.length === 0)) {
    return [];
  }
  return Array.isArray(target) ? target : [target];
}

export default class ArrayTransform extends Transform {
  deserialize(serialized: unknown): unknown[] {
    return transformToArray(serialized);
  }

  serialize(deserialized: unknown): unknown[] {
    return transformToArray(deserialized);
  }
}

