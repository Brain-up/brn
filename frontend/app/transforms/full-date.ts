import { Transform } from '@warp-drive/legacy/serializer/transform';
import { DateTime } from 'luxon';

export default class FullDateTransform extends Transform {
  deserialize(serialized: string | null | undefined): DateTime | null {
    const browserLocale = (navigator && navigator.language) || 'en';
    return (
      (serialized &&
        DateTime.fromISO(serialized, { zone: 'utc', locale: browserLocale })) ||
      null
    );
  }
  serialize(deserialized: DateTime | Date | null | undefined): string | null {
    if (!deserialized) return null;
    if (deserialized instanceof Date) {
      return deserialized.toISOString();
    }
    return deserialized.toISO() ?? null;
  }
}
