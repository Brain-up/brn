import { helper } from '@ember/component/helper';

export function contains([needle, haystack]: [unknown, unknown[] | undefined]): boolean {
  if (!haystack || !Array.isArray(haystack)) return false;
  return haystack.includes(needle);
}

export default helper(contains);
