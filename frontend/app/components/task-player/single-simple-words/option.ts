import Component from '@glimmer/component';
import { action } from '@ember/object';
import { MODES } from 'brn/utils/task-modes';
import UserDataService from 'brn/services/user-data';
import { inject as service } from '@ember/service';
interface ITaskPlayerSingleWordsOptionComponentArguments {
  mode: keyof typeof MODES;
  disableAnswers: boolean;
  isCorrect: boolean;
  activeWord: string;
}
export default class TaskPlayerSingleWordsOptionComponent extends Component<ITaskPlayerSingleWordsOptionComponentArguments> {
  @service('user-data') userData!: UserDataService;
  isClicked = false;
  shouldLoadSymbol(word: string) {
    return word.trim().split(' ').length === 1;
  }
  @action async setDefaultImage(e: Error & { target: HTMLImageElement }) {
    if (e.target.dataset.hasError === '1') {
      e.target.src =
        'data:image/gif;base64,R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';
      return;
    }
    const word = e.target.alt;
    e.target.dataset.hasError = '1';
    e.target.src =
      'data:image/gif;base64,R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';

    if (this.shouldLoadSymbol(word)) {
      const symbols = await fetch(
        'https://www.opensymbols.org/api/v1/symbols/search?q=' +
          encodeURIComponent(word) +
          '&locale=' +
          encodeURIComponent(this.userData.activeLocale.split('-')[0]),
      );
      const data = await symbols.json();
      if (data.length) {
        e.target.crossOrigin = 'anonymous';
        e.target.src = data[0].image_url;
      }
    }
  }
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
        node.style.backgroundColor = '#47CD8A';
        node.style.color = '#fff';
      } else {
        node.style.backgroundColor = '#F38698';
        node.style.color = '#fff';
      }
      this.isClicked = false;
    } else {
      node.setAttribute('style', '');
    }
  }
}
