import { modifier } from 'ember-modifier';
import { run } from '@ember/runloop';
export default modifier(function offsetWidth(element, [cb]) {
  run('next', () => {
    if (typeof cb === 'function') {
      cb(element.offsetWidth, element);
    }
  });
});
