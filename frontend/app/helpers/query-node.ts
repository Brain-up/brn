import { helper } from '@ember/component/helper';

export default helper(function queryNode([selector]: [string]): Element | null {
  return document.querySelector(selector);
});
