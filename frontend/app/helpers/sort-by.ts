import { helper } from '@ember/component/helper';

export function sortBy([key, array]: [string, unknown[]]): unknown[] {
  if (!array || !Array.isArray(array)) return [];
  return [...array].sort((a: any, b: any) => {
    const aVal = a[key];
    const bVal = b[key];
    if (aVal < bVal) return -1;
    if (aVal > bVal) return 1;
    return 0;
  });
}

export default helper(sortBy);
