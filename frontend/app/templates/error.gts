import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';

interface Signature {
  Args: {
    model: any;
  };
}

const tpl: TOC<Signature> = <template>
    oooops...
    <pre>
      {{@model}}
    </pre>
  </template>;

export default RouteTemplate(tpl);
