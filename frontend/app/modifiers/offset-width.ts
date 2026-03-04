import { modifier } from 'ember-modifier';
import { run } from '@ember/runloop';
export default modifier(function offsetWidth(element: HTMLElement, [cb]: [(width: number, el: HTMLElement) => void]) {
  run('next', () => {
    if (typeof cb === 'function') {
      cb(element.offsetWidth, element);
    }
  });
});
