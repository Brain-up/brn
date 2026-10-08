import type { TOC } from '@ember/component/template-only';
import queryNode from 'brn/helpers/query-node';

interface Signature {
  Args: {
    selector: string;
  };
  Blocks: {
    default: [];
  };
}

const SlotTo: TOC<Signature> = <template>
  {{#let (queryNode @selector) as |node|}}
    {{#if node}}
      {{#in-element node}}
        {{yield}}
      {{/in-element}}
    {{/if}}
  {{/let}}
</template>;

export default SlotTo;
