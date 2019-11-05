import LinkComponent from '@ember/routing/link-component';
import { and } from '@ember/object/computed';

export default LinkComponent.extend({
  attributeBindings: ['ariaCurrent:aria-current'],
  putActiveAttr: false,
  ariaCurrent: and('putActiveAttr', 'active'),
});
