import Component from '@glimmer/component';
import { action } from '@ember/object';
export default class LoadingSpinnerComponent extends Component {
  @action fadeOut(node: HTMLDivElement) {
    const animation = node.animate(
      [
        // keyframes
        { opacity: '0' },
        { opacity: '1' },
      ],
      {
        // timing options
        duration: 1000,
      },
    );
    return () => {
      animation.cancel();
    };
  }

  <template>
    <div
      class="flex flex-1 w-full items-center justify-center"
      {{did-insert this.fadeOut}}
    >
      <div class="loader">
        Loading...
      </div>
    </div>
  </template>
}
