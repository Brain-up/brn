import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';

interface Signature {
  Args: {
    model: string;
  };
}

const tpl: TOC<Signature> = <template>
    oooops...
    <pre class="overflow-x-auto whitespace-pre-wrap break-words">
      {{@model}}
    </pre>
  </template>;

export default RouteTemplate(tpl);
