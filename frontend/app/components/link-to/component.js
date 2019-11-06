import LinkComponent from '@ember/routing/link-component';

export default LinkComponent.extend({
  attributeBindings: ['active:aria-current'],
});
