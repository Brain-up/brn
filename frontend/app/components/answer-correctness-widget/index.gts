import './index.css';
import Component from '@glimmer/component';
import { t } from 'ember-intl';
import { not } from 'ember-truth-helpers';
import htmlSafe from 'brn/helpers/html-safe';
import { concat } from '@ember/helper';

// 5 entrance animation themes, randomly picked
const ENTRANCE_THEMES = ['acw-theme-zoom', 'acw-theme-flip', 'acw-theme-drop', 'acw-theme-spiral', 'acw-theme-elastic'] as const;

// 7 success message keys (including the original)
const SUCCESS_KEYS = [
  'exercise_messages.successfully',
  'exercise_messages.successfully_1',
  'exercise_messages.successfully_2',
  'exercise_messages.successfully_3',
  'exercise_messages.successfully_4',
  'exercise_messages.successfully_5',
  'exercise_messages.successfully_6',
] as const;

// 6 celebration SVG hero icons (data URIs for correct rendering)
function svgUri(vb: string, content: string): string {
  return `data:image/svg+xml,${encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="${vb}">${content}</svg>`)}`;
}

const CELEBRATION_HEROES = [
  // trophy
  svgUri('0 0 80 80',
    '<path d="M20 12h40v6H20z" fill="#fbbf24"/>' +
    '<path d="M24 18h32v24c0 10-7 17-16 17s-16-7-16-17V18z" fill="#fbbf24" stroke="#f59e0b" stroke-width="2"/>' +
    '<path d="M24 24c-8 0-13 5-13 10s5 10 10 10c3 0 5-1 5-1" fill="none" stroke="#f59e0b" stroke-width="2.5" stroke-linecap="round"/>' +
    '<path d="M56 24c8 0 13 5 13 10s-5 10-10 10c-3 0-5-1-5-1" fill="none" stroke="#f59e0b" stroke-width="2.5" stroke-linecap="round"/>' +
    '<rect x="33" y="59" width="14" height="5" rx="1.5" fill="#f59e0b"/>' +
    '<rect x="28" y="64" width="24" height="5" rx="2" fill="#fbbf24" stroke="#f59e0b" stroke-width="1.5"/>' +
    '<polygon points="40,26 43,34 51,35 45,41 47,49 40,44 33,49 35,41 29,35 37,34" fill="#fff" opacity="0.9"/>'),
  // star burst
  svgUri('0 0 80 80',
    '<polygon points="40,6 47,28 70,28 52,42 58,64 40,50 22,64 28,42 10,28 33,28" fill="#fbbf24" stroke="#f59e0b" stroke-width="1.5"/>' +
    '<polygon points="40,20 43,30 54,30 46,36 48,46 40,40 32,46 34,36 26,30 37,30" fill="#fff" opacity="0.5"/>'),
  // medal with ribbon
  svgUri('0 0 80 80',
    '<path d="M30 8l10 28h-10z" fill="#ef4444" opacity="0.8"/>' +
    '<path d="M50 8l-10 28h10z" fill="#3b82f6" opacity="0.8"/>' +
    '<circle cx="40" cy="48" r="20" fill="#fbbf24" stroke="#f59e0b" stroke-width="2"/>' +
    '<circle cx="40" cy="48" r="14" fill="none" stroke="#f59e0b" stroke-width="1.5"/>' +
    '<polygon points="40,36 43,44 51,44 45,49 47,57 40,52 33,57 35,49 29,44 37,44" fill="#f59e0b"/>'),
  // rocket launch
  svgUri('0 0 80 80',
    '<path d="M40 8c-6 8-9 18-9 28l9 12 9-12c0-10-3-20-9-28z" fill="#a78bfa" stroke="#8b5cf6" stroke-width="1.5"/>' +
    '<path d="M31 36l-8 12 8 4z" fill="#f97316"/>' +
    '<path d="M49 36l8 12-8 4z" fill="#f97316"/>' +
    '<circle cx="40" cy="28" r="4" fill="#fff"/>' +
    '<path d="M36 48l-2 16c0 0 4 6 6 8 2-2 6-8 6-8l-2-16" fill="#ef4444" opacity="0.8"/>' +
    '<path d="M38 48l-1 12c0 0 2 4 3 5 1-1 3-5 3-5l-1-12" fill="#fbbf24" opacity="0.9"/>'),
  // crown
  svgUri('0 0 80 80',
    '<path d="M10 52l10-28 20 16 20-16 10 28z" fill="#fbbf24" stroke="#f59e0b" stroke-width="2"/>' +
    '<rect x="10" y="52" width="60" height="12" rx="3" fill="#fbbf24" stroke="#f59e0b" stroke-width="2"/>' +
    '<circle cx="20" cy="24" r="4" fill="#ef4444"/>' +
    '<circle cx="40" cy="22" r="5" fill="#3b82f6"/>' +
    '<circle cx="60" cy="24" r="4" fill="#22c55e"/>' +
    '<circle cx="26" cy="56" r="2.5" fill="#ef4444" opacity="0.7"/>' +
    '<circle cx="40" cy="56" r="2.5" fill="#3b82f6" opacity="0.7"/>' +
    '<circle cx="54" cy="56" r="2.5" fill="#22c55e" opacity="0.7"/>'),
  // thumbs up
  svgUri('0 0 80 80',
    '<path d="M28 72h-10c-2 0-4-2-4-4V44c0-2 2-4 4-4h10v32z" fill="#60a5fa"/>' +
    '<path d="M28 40h16c4 0 8-8 10-16 1-4 6-4 6 0v14h8c3 0 5 3 4 6l-6 24c-1 3-3 4-6 4H28V40z" fill="#fbbf24" stroke="#f59e0b" stroke-width="1.5"/>'),
];

// Confetti colors
const CONFETTI_COLORS = [
  '#ff6b6b', '#feca57', '#48dbfb', '#ff9ff3', '#54a0ff',
  '#5f27cd', '#01a3a4', '#f368e0', '#ff6348', '#7bed9f',
  '#fd79a8', '#00cec9',
];

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

  // Randomized per-instance
  entranceTheme = ENTRANCE_THEMES[Math.floor(Math.random() * ENTRANCE_THEMES.length)];
  successMessageKey = SUCCESS_KEYS[Math.floor(Math.random() * SUCCESS_KEYS.length)];
  heroIcon = CELEBRATION_HEROES[Math.floor(Math.random() * CELEBRATION_HEROES.length)];

  // Confetti burst from two sides
  confetti = Array.from({ length: 40 }, (_, i) => {
    const fromLeft = i < 20;
    const hSpread = 15 + Math.random() * 70;
    const vSpread = 30 + Math.random() * 60;
    const wobble = (Math.random() - 0.5) * 25;
    const color = CONFETTI_COLORS[i % CONFETTI_COLORS.length];
    const size = 4 + Math.random() * 7;
    const delay = Math.random() * 0.6;
    const duration = 1.2 + Math.random() * 1.3;
    const rotation = Math.random() * 720;
    const shape = i % 3 === 0 ? 'circle' : i % 3 === 1 ? 'square' : 'strip';
    return {
      id: i,
      shape,
      style: [
        `--h-spread:${fromLeft ? hSpread : -hSpread + 100}%`,
        `--v-spread:${vSpread}%`,
        `--wobble:${wobble}px`,
        `--rotation:${rotation}deg`,
        `--confetti-color:${color}`,
        `left:${fromLeft ? -2 : 102}%`,
        `bottom:10%`,
        `animation-delay:${delay}s`,
        `animation-duration:${duration}s`,
        `width:${size}px`,
        `height:${shape === 'strip' ? size * 2.5 : size}px`,
        `background:${color}`,
      ].join(';'),
    };
  });

  // Sparkle bursts around hero icon
  sparkles = Array.from({ length: 10 }, (_, i) => {
    const angle = (i / 10) * 360;
    const dist = 80 + Math.random() * 60;
    const delay = 0.2 + Math.random() * 0.6;
    const size = 4 + Math.random() * 6;
    return {
      id: i,
      style: `--spark-angle:${angle}deg;--spark-dist:${dist}px;animation-delay:${delay}s;width:${size}px;height:${size}px;`,
    };
  });

  get imagePath() {
    const randomNumber = this.getAllowedRandomNumber();
    return `${
      this.args.isCorrect ? 'victory/victory' : 'regret/regret'
    }${randomNumber}`;
  }

  getAllowedRandomNumber(): number {
    return getRandomInt(1, this.maxImagesNumber);
  }

  <template>
    <div
      class="c-answer-correctness-widget
        {{if @isCorrect 'c-answer-correctness-widget--correct' 'c-answer-correctness-widget--incorrect'}}
        {{if @isCorrect this.entranceTheme ''}}
        flex flex-wrap flex-1 flex-col text-center justify-evenly pb-0 items-center rounded-large"
      ...attributes
    >
      {{#if @isCorrect}}
        {{!-- ===== Confetti cannon ===== --}}
        <div class="acw__confetti" aria-hidden="true">
          {{#each this.confetti as |piece|}}
            <div
              class="acw__confetti-piece acw__confetti-piece--{{piece.shape}}"
              style={{htmlSafe piece.style}}
            ></div>
          {{/each}}
        </div>

        {{!-- ===== Sparkle bursts ===== --}}
        <div class="acw__sparkles" aria-hidden="true">
          {{#each this.sparkles as |spark|}}
            <div class="acw__sparkle" style={{htmlSafe spark.style}}></div>
          {{/each}}
        </div>

        {{!-- ===== Pulsing glow background ===== --}}
        <div class="acw__glow" aria-hidden="true"></div>

        {{!-- ===== Varied congratulation text ===== --}}
        <h3 class="acw__title">
          {{t this.successMessageKey}}
        </h3>

        {{!-- ===== Celebration hero icon (randomized) ===== --}}
        <div
          data-test-answer-correctness-widget
          data-test-is-correct={{@isCorrect}}
          data-test-isnt-correct={{not @isCorrect}}
          class="acw__hero"
        >
          <img
            src={{this.heroIcon}}
            alt=""
            class="acw__hero-icon"
          />
        </div>
      {{else}}
        {{!-- ===== Incorrect: encouraging ===== --}}
        <h3 class="acw__title acw__title--incorrect">
          {{t "exercise_messages.unsuccessfully"}}
        </h3>
        <div
          data-test-answer-correctness-widget
          data-test-is-correct={{@isCorrect}}
          data-test-isnt-correct={{not @isCorrect}}
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
