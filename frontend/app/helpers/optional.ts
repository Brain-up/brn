import { helper } from '@ember/component/helper';

export function optional([action]: [Function | undefined]): Function {
  if (typeof action === 'function') {
    return action;
  }
  return () => {};
}

export default helper(optional);
