import type { TOC } from '@ember/component/template-only';
import isServerError from 'brn/utils/is-server-error';
import ServerDown from 'brn/components/server-down';

interface Signature {
  Args: {
    model: unknown;
  };
}

function errorMessage(error: unknown): string {
  if (error instanceof Error) return error.message;
  return String(error ?? '');
}

const ErrorPage: TOC<Signature> = <template>
  {{#if (isServerError @model)}}
    <ServerDown />
  {{else}}
    oooops...
    <pre class="overflow-x-auto whitespace-pre-wrap break-words">
      {{errorMessage @model}}
    </pre>
  {{/if}}
</template>;

export default ErrorPage;
