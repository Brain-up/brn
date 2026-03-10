import { helper } from '@ember/component/helper';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function groupBy([property, items]: [string, any[]]): Record<string, any[]> {
  const result: Record<string, any[]> = {}; // eslint-disable-line @typescript-eslint/no-explicit-any
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
