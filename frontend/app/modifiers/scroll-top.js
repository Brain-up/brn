import { modifier } from 'ember-modifier';

export default modifier(function scrollTop(element, [enabled]) {
  if (enabled) {
    element.scrollIntoView({behavior: "smooth", block: "start", inline: "nearest"});
  }
});
