import type { TOC } from '@ember/component/template-only';

interface Signature {
  Args: {
    class?: string;
  };
  Element: HTMLDivElement;
}

const SkeletonCircle: TOC<Signature> = <template>
  <div class="skeleton-circle {{@class}}" ...attributes></div>
</template>;

export default SkeletonCircle;
