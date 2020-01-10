import { helper as buildHelper } from '@ember/component/helper';

export function capitalize([value]) {
  value = (value && value.string) || value;
  return value && value[0].toUpperCase() + value.slice(1);
}

export default buildHelper(capitalize);
