import type { TOC } from '@ember/component/template-only';
import UiTabButton from 'brn/components/ui/tab-button';

interface Signature {
  Args: {
    mode: 'enabled' | 'disabled' | 'active';
  };
  Blocks: {
    default: [];
  };
  Element: HTMLDivElement;
}

const ExerciseStepsStep: TOC<Signature> = <template>
  <div class="flex flex-1 w-full">
    <UiTabButton ...attributes @mode={{@mode}}>{{yield}}</UiTabButton>
  </div>
</template>;

export default ExerciseStepsStep;
