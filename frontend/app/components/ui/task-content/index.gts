import type { TOC } from '@ember/component/template-only';
import htmlSafe from 'brn/helpers/html-safe';

interface Signature {
  Blocks: {
    default: [];
  };
  Element: HTMLDivElement;
}

const UiTaskContent: TOC<Signature> = <template>
  <div style={{htmlSafe "flex: 1 0 auto;"}}>
    {{yield}}
  </div>
</template>;

export default UiTaskContent;
