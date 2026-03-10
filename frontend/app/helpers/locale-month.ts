import Helper from '@ember/component/helper';
import { service } from '@ember/service';
import type IntlService from 'ember-intl/services/intl';
import type { DateTime } from 'luxon';

export default class LocaleMonthHelper extends Helper<{
  Args: { Positional: [DateTime]; Named: { format?: string } };
  Return: string;
}> {
  @service('intl') intl!: IntlService;

  compute([date]: [DateTime], { format }: { format?: string } = {}): string {
    return date.reconfigure({ locale: this.intl.primaryLocale }).toFormat(format ?? 'LLLL');
  }
}
