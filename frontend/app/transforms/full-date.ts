// eslint-disable-next-line ember/use-ember-data-rfc-395-imports
import DS from 'ember-data';
import { DateTime } from 'luxon';
const { DateTransform } = DS;
class FullDate extends DateTransform {
  deserialize(serialized: Record<string, never>): DateTime | null {
    //
    const browserLocale  = (navigator && navigator.language) || 'en';
    return (
      (serialized &&
        DateTime.fromISO(serialized as unknown as string, { zone: 'utc', locale: browserLocale })) ||
      null
    );
  }
}

declare module 'ember-data/types/registries/transform' {
  export default interface TransformRegistry {
    // eslint-disable-next-line
    'full-date': typeof FullDate;
  }
}

export default FullDate;
