import { helper } from '@ember/component/helper';

export default helper(function sum([first, second]: [number, number]): number {
  return first + second;
});
