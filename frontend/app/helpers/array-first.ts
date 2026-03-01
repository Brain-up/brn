import { helper } from '@ember/component/helper';

export function arrayFirst([arr]: [unknown[]]): unknown {
  if (!arr || !arr.length) return undefined;
  return arr[0];
}

export default helper(arrayFirst);
