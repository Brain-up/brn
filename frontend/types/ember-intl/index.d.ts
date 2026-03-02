export { default as Service } from './services/intl';

import Helper from '@ember/component/helper';

interface TSignature {
  Args: {
    Named?: Record<string, unknown>;
    Positional: [string] | [string, Record<string, unknown>];
  };
  Return: string;
}

export class t extends Helper<TSignature> {
  compute(
    [key, positionalOptions]: TSignature['Args']['Positional'],
    namedOptions: TSignature['Args']['Named'],
  ): string;
}
