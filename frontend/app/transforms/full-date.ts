// eslint-disable-next-line ember/use-ember-data-rfc-395-imports
import DS from 'ember-data';
import { DateTime } from 'luxon';

const { DateTransform } = DS;
const FullDate = DateTransform.extend({
  deserialize(serialized: Record<string, never>): DateTime | null {
    return (
      (serialized &&
        DateTime.fromISO(serialized as unknown as string, { zone: 'utc' })) ||
      null
    );
  },
});

declare module 'ember-data/types/registries/transform' {
  export default interface TransformRegistry {
    // eslint-disable-next-line
    'full-date': FullDate;
  }
}

export default FullDate;
