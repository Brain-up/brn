// eslint-disable-next-line ember/use-ember-data-rfc-395-imports
import DS from 'ember-data';
import { DateTime } from 'luxon';
import { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';
const { DateTransform } = DS;
const FullDate = DateTransform.extend({
  userData: service('user-data'),
  deserialize(serialized: Record<string, never>): DateTime | null {
    const locale = (this.userData as unknown as UserDataService).activeLocale as unknown as string
    return (
      (serialized &&
        DateTime.fromISO(serialized as unknown as string, { zone: 'utc', locale })) ||
      null
    );
  },
});

declare module 'ember-data/types/registries/transform' {
  export default interface TransformRegistry {
    // eslint-disable-next-line
    'full-date': typeof FullDate;
  }
}

export default FullDate;
