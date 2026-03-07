import Component from '@glimmer/component';
import { action } from '@ember/object';
import { MODES, type Mode } from 'brn/utils/task-modes';
import { service } from '@ember/service';
import ImageLocatorService from 'brn/services/image-locator';
import StudyConfigService from 'brn/services/study-config';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { eq } from 'ember-truth-helpers';
import { and } from 'ember-truth-helpers';

interface TaskPlayerSingleWordsOptionSignature {
  Args: {
  mode: Mode;
  disableAnswers: boolean;
  isCorrect: boolean;
  activeWord: string;
  answerOption: import('brn/utils/answer-option').default;
  onPlayText: (word: string) => void;
  checkMaybe: (word: string) => void;
  };
  Element: HTMLElement;
}

export default class TaskPlayerSingleWordsOptionComponent extends Component<TaskPlayerSingleWordsOptionSignature> {
  @service('image-locator') imageLocator!: ImageLocatorService;
  @service('study-config') studyConfig!: StudyConfigService;
  isClicked = false;
  shouldLoadSymbol(word: string) {
    if (!this.studyConfig.showImages) {
      return false;
    }

    return word.trim().split(' ').length === 1;
  }
  @action async setDefaultImage(e: ErrorEvent & { target: HTMLImageElement }) {
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

  <template>
    <li class="task-player__option" ...attributes>
      <button
        data-test-task-answer
        data-test-task-answer-option={{@answerOption.word}}
        disabled={{this.isDisabled}}
        type="button"
        {{didUpdate this.handleAnswer @isCorrect}}
        class="btn-press task-player__option-button py-1 sm:px-2 rounded
          {{if
            (eq @activeWord @answerOption.word)
            "border-2 text-white bg-purple-primary"
            "border-2 border-purple-primary/25 text-purple-primary bg-transparent"
          }}
          {{if
            @disableAnswers
            "opacity-50 cursor-default"
            "hover:bg-purple-primary hover:text-white hover:border-transparent"
          }}"
        {{on
          "click"
          (if
            (eq @mode "interact")
            (fn @onPlayText @answerOption.word)
            (fn this.handleClick (fn @checkMaybe @answerOption.word))
          )
        }}
      >
        {{#if (and this.studyConfig.showImages)}}
          <div class="sm:w-24 w-20 m-auto">
            <img
              src={{@answerOption.pictureFileUrl}}
              alt={{@answerOption.word}}
              {{on "error" this.setDefaultImage}}
            />
          </div>
        {{/if}}
        {{@answerOption.word}}
      </button>
    </li>
  </template>
}
