import Component from '@glimmer/component';
import { action } from '@ember/object';
import { MODES } from 'brn/utils/task-modes';
import { inject as service } from '@ember/service';
import ImageLocatorService from 'brn/services/image-locator';
import StudyConfigService from 'brn/services/study-config';



interface ITaskPlayerSingleWordsOptionComponentArguments {
  mode: keyof typeof MODES;
  disableAnswers: boolean;
  isCorrect: boolean;
  activeWord: string;
}
export default class TaskPlayerSingleWordsOptionComponent extends Component<ITaskPlayerSingleWordsOptionComponentArguments> {
  @service('image-locator') imageLocator!: ImageLocatorService;
  @service('study-config') studyConfig!: StudyConfigService;
  isClicked = false;
  shouldLoadSymbol(word: string) {
    if (!this.studyConfig.showImages) { 
      return false;
    }
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
      const url = await this.imageLocator.getPictureForWord(word);
      if (url) {
        e.target.crossOrigin = 'anonymous';
        e.target.src = url;
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
