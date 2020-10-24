import Component from '@glimmer/component';
import { action } from '@ember/object';
export default class TaskPlayerSingleWordsOptionComponent extends Component {
  isClicked = false;
  @action handleClick(cb: any) {
    this.isClicked = true;
    cb();
  }
  @action handleAnswer(node: HTMLButtonElement, [isCorrect]: [boolean]) {
    if (this.isClicked) {
      if (isCorrect) {
        node.style.backgroundColor = 'green';
      } else {
        node.style.backgroundColor = 'red';
      }
      this.isClicked = false;
    } else {
      node.setAttribute('style', '');
    }
  }
}
