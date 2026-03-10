import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
import isServerError from 'brn/utils/is-server-error';
import ServerDown from 'brn/components/server-down';

interface Signature {
  Args: {
    model: unknown;
  };
}

const tpl: TOC<Signature> = <template>
    {{#if (isServerError @model)}}
      <ServerDown />
    {{else}}
      oooops...
      <pre class="overflow-x-auto whitespace-pre-wrap break-words">
        {{@model}}
      </pre>
    {{/if}}
  </template>;

export default RouteTemplate(tpl);
