import { modifier } from 'ember-modifier';

export default modifier(function scrollTop(element: HTMLElement, [enabled]: [boolean]) {
  if (enabled) {
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
      inline: 'nearest',
    });
  }
});
