import './index.css';
import Component from '@glimmer/component';
import { t } from 'ember-intl';
import { not } from 'ember-truth-helpers';
import htmlSafe from 'brn/helpers/html-safe';
import { concat } from '@ember/helper';
import UiIconCorrectAnswer from 'brn/components/ui/icon/correct-answer';

interface AnswerCorrectnessWidgetSignature {
  Args: {
  isCorrect: boolean;
  };
  Element: HTMLElement;
}

function getRandomInt(min: number, max: number) {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default class AnswerCorrectnessWidgetComponent extends Component<AnswerCorrectnessWidgetSignature> {
  maxImagesNumber = 8;
  get imagePath() {
    const randomNumber = this.getAllowedRandomNumber();
    return `${
      this.args.isCorrect ? 'victory/victory' : 'regret/regret'
    }${randomNumber}`;
  }
  getAllowedRandomNumber(): number {
    return getRandomInt(1, this.maxImagesNumber);
  }
  get itemStyle() {
    const img = this.imagePath;
    return `
      background-image:
        url('/pictures/${img}.jpg'),
        url('/pictures/${img}.png'),
        url('/pictures/${img}.jpeg'),
        url('/pictures/${img}.svg');
    `.trim();
  }

  <template>
    <div
      class="c-answer-correctness-widget
        flex flex-wrap flex-1 flex-col text-center justify-evenly pb-0 items-center rounded-large"
      ...attributes
    >
      <h3 class="mt-6 mb-6 text-2xl font-semibold">
        {{if
          @isCorrect
          (t "exercise_messages.successfully")
          (t "exercise_messages.unsuccessfully")
        }}
      </h3>
      {{#if @isCorrect}}
        <div
          data-test-answer-correctness-widget
          data-test-is-correct={{@isCorrect}}
          data-test-isnt-correct={{not @isCorrect}}
          class="object-center"
        >
          <UiIconCorrectAnswer />
        </div>
      {{else}}
        {{! template-lint-configure no-inline-styles false }}
        <div
          data-test-answer-correctness-widget
          data-test-is-correct={{@isCorrect}}
          data-test-isnt-correct={{not @isCorrect}}
          aria-label={{htmlSafe (concat (t "exercise_messages.successfully"))}}
          style={{htmlSafe
            (concat
              (concat "background-image: url('/pictures/" this.imagePath ".jpg')")
              ", "
              (concat "url('/pictures/" this.imagePath ".png')")
              ", "
              (concat "url('/pictures/" this.imagePath ".jpeg')")
              ", "
              (concat "url('/pictures/" this.imagePath ".svg')")
            )
          }}
          class="c-answer-correctness-widget__content flex-1 mx-auto rounded"
        ></div>
      {{/if}}
    </div>
  </template>
}
