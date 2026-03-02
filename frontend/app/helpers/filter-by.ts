import { helper } from '@ember/component/helper';

export function filterBy([key, value, array]: [string, unknown, unknown[]]): unknown[] {
  if (!array || !Array.isArray(array)) return [];
  return array.filter((item: any) => item[key] === value);
}

export default helper(filterBy);
