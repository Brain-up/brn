import type { TOC } from '@ember/component/template-only';

interface Signature {
  Args: {
    class?: string;
  };
  Element: HTMLDivElement;
}

const SkeletonLine: TOC<Signature> = <template>
  <div class="skeleton-pulse {{@class}}" ...attributes></div>
</template>;

export default SkeletonLine;
