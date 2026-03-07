import { helper } from '@ember/component/helper';

export function groupBy([property, items]: [string, any[]]): Record<string, any[]> {
  const result: Record<string, any[]> = {};
  if (!items || !property) return result;
  for (const item of items) {
    const key = String(item[property] ?? 'undefined');
    if (!result[key]) {
      result[key] = [];
    }
    result[key].push(item);
  }
  return result;
}

export default helper(groupBy);
