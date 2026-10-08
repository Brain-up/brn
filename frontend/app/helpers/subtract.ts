import { helper } from '@ember/component/helper';

export default helper(function subtract([minuend, subtrahend = 0]: [number, number?]): number {
  return minuend - subtrahend;
});
