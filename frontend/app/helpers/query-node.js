import { helper } from '@ember/component/helper';

export default helper(function queryNode([selector] /*, hash*/) {
  return document.querySelector(selector);
});
