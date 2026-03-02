import { helper } from '@ember/component/helper';

export function range([start, end, inclusive]: [number, number, boolean?]): number[] {
  const result: number[] = [];
  const limit = inclusive ? end : end - 1;
  for (let i = start; i <= limit; i++) {
    result.push(i);
  }
  return result;
}

export default helper(range);
