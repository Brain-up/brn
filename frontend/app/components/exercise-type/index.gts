import './index.css';
import type { TOC } from '@ember/component/template-only';

interface Signature {
  Args: {
    img: string;
    initial: string;
  };
  Element: HTMLDivElement;
}

const ExerciseType: TOC<Signature> = <template>
  <div class="c-exercise-type hover:ring shadow-xl">
    <button class="btn-press card flex flex-col bg-white" type="button">
      <img class="card-img" src="/pictures/exercise-type/{{@img}}.png" alt="{{@initial}}" />
      <h4 class="w-full mb-2 text-sm font-medium text-center text-gray-700 uppercase">
        {{@initial}}
      </h4>
    </button>
  </div>
</template>;

export default ExerciseType;
