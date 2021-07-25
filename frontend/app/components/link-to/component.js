import LinkComponent from '@ember/routing/link-component';

// eslint-disable-next-line ember/no-classic-classes
export default LinkComponent.extend({
  attributeBindings: ['active:aria-current', 'disabled'],
});
