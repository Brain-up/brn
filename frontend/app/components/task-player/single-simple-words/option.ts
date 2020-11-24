import Component from '@glimmer/component';
import { action } from '@ember/object';
import { MODES } from 'brn/utils/task-modes';

interface ITaskPlayerSingleWordsOptionComponentArguments {
  mode: keyof (typeof MODES),
  disableAnswers: boolean,
  isCorrect: boolean,
  activeWord: string
}
export default class TaskPlayerSingleWordsOptionComponent extends Component<ITaskPlayerSingleWordsOptionComponentArguments> {
  isClicked = false;
  @action handleClick(cb: any) {
    this.isClicked = true;
    cb();
  }
  get isDisabled() {
    return this.args.disableAnswers || this.args.mode === MODES.LISTEN || false;
  }
  @action handleAnswer(node: HTMLButtonElement, [isCorrect]: [boolean]) {
    if (this.args.mode !== MODES.TASK) {
      return;
    }
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
