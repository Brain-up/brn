import Component from '@glimmer/component';
import { action } from '@ember/object';
export default class LoadingSpinnerComponent extends Component {
  @action fadeOut(node) {
    let animation = node.animate(
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
}
