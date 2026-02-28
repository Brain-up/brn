import { helper } from '@ember/component/helper';

export function arrayLast([arr]) {
  if (!arr || !arr.length) return undefined;
  return arr[arr.length - 1];
}

export default helper(arrayLast);
