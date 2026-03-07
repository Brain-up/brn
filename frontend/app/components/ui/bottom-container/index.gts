import './index.css';
import type { TOC } from '@ember/component/template-only';

interface BottomContainerSignature {
  Args: {};
  Blocks: { default: [] };
  Element: HTMLDivElement;
}

const BottomContainer: TOC<BottomContainerSignature> = <template>
  <div
    ...attributes
    class="c-bottom-container w-full my-4"
  >
    {{yield}}
  </div>
</template>;

export default BottomContainer;
