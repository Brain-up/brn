import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import { IStatsExerciseStats } from 'brn/services/stats';
import type GamificationService from 'brn/services/gamification';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import { or } from 'ember-truth-helpers';
import htmlSafe from 'brn/helpers/html-safe';
import UiButton from 'brn/components/ui/button';

// 5 distinct animation themes — randomly picked per completion
const ANIMATION_THEMES = ['theme-bounce', 'theme-flip', 'theme-swing', 'theme-pop', 'theme-spiral'] as const;

// 5 distinct background color palettes — randomized independently of theme
const BG_PALETTES = [
  'palette-aurora',   // green/blue/purple
  'palette-sunset',   // orange/pink/purple
  'palette-ocean',    // teal/blue/indigo
  'palette-candy',    // pink/yellow/mint
  'palette-galaxy',   // deep purple/blue/magenta
] as const;

const CONFETTI_COLORS = [
  '#ff6b6b', '#feca57', '#48dbfb', '#ff9ff3', '#54a0ff',
  '#5f27cd', '#01a3a4', '#f368e0', '#ff6348', '#7bed9f',
  '#fd79a8', '#00cec9', '#e17055', '#74b9ff', '#a29bfe',
];

// Pre-encoded SVG data URIs for floating celebration icons (avoids SVG namespace issues with htmlSafe)
function svgDataUri(viewBox: string, content: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${viewBox}">${content}</svg>`;
  return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

const CELEBRATION_ICONS = [
  svgDataUri('0 0 20 20', '<path d="M10 1l2.5 5 5.5 1-4 3.8 1 5.5L10 13.5 4.9 16.3l1-5.5L2 7l5.5-1z" fill="#fbbf24"/>'),
  svgDataUri('0 0 20 20', '<path d="M10 18s-7-5.3-7-10A4 4 0 0110 5.5 4 4 0 0117 8c0 4.7-7 10-7 10z" fill="#f472b6"/>'),
  svgDataUri('0 0 20 20', '<path d="M11 1L4 11h5l-1 8 7-10h-5z" fill="#facc15"/>'),
  svgDataUri('0 0 20 20', '<path d="M10 2l8 8-8 8-8-8z" fill="#60a5fa"/>'),
  svgDataUri('0 0 20 20', '<path d="M5 3h10v2h-10z" fill="#fbbf24"/><path d="M6 5h8v6c0 3-2 4-4 4s-4-1-4-4V5z" fill="#fbbf24"/><rect x="8" y="15" width="4" height="2" rx="0.5" fill="#f59e0b"/>'),
  svgDataUri('0 0 20 20', '<path d="M10 2c-2 3-3 6-3 9l3 4 3-4c0-3-1-6-3-9z" fill="#a78bfa"/><path d="M7 11l-2 3 2 1z" fill="#f97316"/><path d="M13 11l2 3-2 1z" fill="#f97316"/><circle cx="10" cy="9" r="1.5" fill="#fff"/>'),
  svgDataUri('0 0 20 20', '<path d="M10 2C10 2 5 7 5 11a5 5 0 0010 0c0-4-5-9-5-9z" fill="#f97316"/><path d="M10 8c0 0-2 2.5-2 4.5a2 2 0 004 0C12 10.5 10 8 10 8z" fill="#fbbf24"/>'),
  svgDataUri('0 0 20 20', '<path d="M2 14l3-8 5 4 5-4 3 8z" fill="#fbbf24" stroke="#f59e0b" stroke-width="0.5"/><rect x="2" y="14" width="16" height="3" rx="1" fill="#fbbf24" stroke="#f59e0b" stroke-width="0.5"/>'),
  svgDataUri('0 0 20 20', '<path d="M10 1l1.5 6.5L18 10l-6.5 1.5L10 18l-1.5-6.5L2 10l6.5-2.5z" fill="#c084fc"/>'),
  svgDataUri('0 0 20 20', '<circle cx="6" cy="14" r="3" fill="#34d399"/><circle cx="14" cy="12" r="3" fill="#34d399"/><path d="M9 14V4l8-2v10" fill="none" stroke="#34d399" stroke-width="2"/>'),
];

interface ExerciseStatsSignature {
  Args: {
  stats: IStatsExerciseStats;
  onComplete: () => void;
  };
  Element: HTMLElement;
}

export default class ExerciseStatsComponent extends Component<ExerciseStatsSignature> {
  @service('gamification') declare gamification: GamificationService;

  // Randomly pick animation theme + background palette each time
  animationTheme = ANIMATION_THEMES[Math.floor(Math.random() * ANIMATION_THEMES.length)];
  bgPalette = BG_PALETTES[Math.floor(Math.random() * BG_PALETTES.length)];

  // === CONFETTI CANNON: two bursts from bottom corners ===
  confettiPieces = Array.from({ length: 50 }, (_, i) => {
    const fromLeft = i < 25; // first half from left, second from right
    const hSpread = 20 + Math.random() * 60; // horizontal spread
    const vSpread = 40 + Math.random() * 50; // vertical height
    const wobble = (Math.random() - 0.5) * 30; // zigzag
    const color = CONFETTI_COLORS[i % CONFETTI_COLORS.length];
    const size = 4 + Math.random() * 8;
    const delay = (fromLeft ? 0 : 0.15) + Math.random() * 0.5;
    const duration = 1.5 + Math.random() * 1.5;
    const rotation = Math.random() * 720;
    const shape = i % 4 === 0 ? 'circle' : i % 4 === 1 ? 'square' : i % 4 === 2 ? 'strip' : 'triangle';

    return {
      id: i,
      shape,
      style: [
        `--h-spread:${fromLeft ? hSpread : -hSpread + 100}%`,
        `--v-spread:${vSpread}%`,
        `--wobble:${wobble}px`,
        `--rotation:${rotation}deg`,
        `left:${fromLeft ? -2 : 102}%`,
        `bottom:0`,
        `animation-delay:${delay}s`,
        `animation-duration:${duration}s`,
        `width:${size}px`,
        `height:${shape === 'strip' ? size * 2.5 : size}px`,
        `background:${color}`,
        `--confetti-color:${color}`,
      ].join(';'),
    };
  });

  // === FIREWORK BURSTS: 3 explosions at different positions ===
  fireworks = [
    { id: 0, left: '20%', top: '15%', delay: '0.3s', hue: '0' },
    { id: 1, left: '75%', top: '10%', delay: '0.7s', hue: '200' },
    { id: 2, left: '50%', top: '25%', delay: '1.1s', hue: '45' },
  ].map(fw => ({
    ...fw,
    style: `left:${fw.left};top:${fw.top};animation-delay:${fw.delay};--fw-hue:${fw.hue};`,
    // 12 particles per firework
    particles: Array.from({ length: 12 }, (_, i) => {
      const angle = (i / 12) * 360;
      const dist = 30 + Math.random() * 40;
      return {
        id: i,
        style: `--angle:${angle}deg;--dist:${dist}px;animation-delay:${fw.delay};`,
      };
    }),
  }));

  // === FLOATING SVG ICONS: varied motion (using <img> with data URIs for correct SVG rendering) ===
  floatingIcons = Array.from({ length: 10 }, (_, i) => {
    const left = 5 + Math.random() * 90;
    const delay = 0.2 + Math.random() * 2.5;
    const duration = 2 + Math.random() * 2.5;
    const wobbleAmount = 15 + Math.random() * 30;
    const spin = (Math.random() > 0.5 ? 1 : -1) * (180 + Math.random() * 360);
    const src = CELEBRATION_ICONS[Math.floor(Math.random() * CELEBRATION_ICONS.length)];
    return {
      id: i,
      src,
      style: `left:${left}%;animation-delay:${delay}s;animation-duration:${duration}s;--wobble:${wobbleAmount}px;--spin:${spin}deg;`,
    };
  });

  // === SHOOTING STARS: diagonal streaks ===
  shootingStars = Array.from({ length: 4 }, (_, i) => ({
    id: i,
    style: `top:${10 + Math.random() * 40}%;animation-delay:${0.5 + i * 0.8 + Math.random() * 0.5}s;--star-length:${60 + Math.random() * 80}px;`,
  }));

  // === SPARKLES around the ring ===
  sparkles = Array.from({ length: 12 }, (_, i) => {
    const angle = (i / 12) * Math.PI * 2;
    const radius = 48;
    return {
      id: i,
      cx: 48 + Math.cos(angle) * radius,
      cy: 48 + Math.sin(angle) * radius,
      style: `animation-delay:${i * 0.12}s;`,
    };
  });

  // === RIPPLE WAVES from center ===
  ripples = Array.from({ length: 3 }, (_, i) => ({
    id: i,
    style: `animation-delay:${0.2 + i * 0.5}s;`,
  }));

  get stats(): IStatsExerciseStats {
    return this.args.stats || {};
  }

  get sessionXp() {
    return this.gamification.sessionXp;
  }

  get accuracy() {
    const { rightAnswersCount, wrongAnswersCount } = this.stats;
    const total = (rightAnswersCount || 0) + (wrongAnswersCount || 0);
    if (total === 0) return 100;
    return Math.round(((rightAnswersCount || 0) / total) * 100);
  }

  get streakStatus() {
    return this.gamification.currentStreak;
  }

  get currentLevel() {
    return this.gamification.level;
  }

  get growthMessageKey() {
    if (this.accuracy >= 90) return 'gamification.summary_excellent';
    if (this.accuracy >= 60) return 'gamification.summary_good';
    return 'gamification.summary_keep_going';
  }

  get timeStats() {
    const { countedSeconds } = this.stats;
    const ms = countedSeconds * 1000;
    const totalSec = Math.floor(ms / 1000);
    const sec = totalSec % 60;
    const min = Math.floor(totalSec / 60);
    return { min, sec };
  }

  get accuracyClass() {
    if (this.accuracy >= 90) return 'exercise-stats__accuracy--excellent';
    if (this.accuracy >= 60) return 'exercise-stats__accuracy--good';
    return 'exercise-stats__accuracy--needs-work';
  }

  get accuracyRingColor() {
    if (this.accuracy >= 90) return '#22c55e';
    if (this.accuracy >= 60) return '#eab308';
    return '#ef4444';
  }

  get accuracyCircumference() {
    return 2 * Math.PI * 40;
  }

  get accuracyDashoffset() {
    return this.accuracyCircumference * (1 - this.accuracy / 100);
  }

  get levelProgress() {
    return this.gamification.xpProgress;
  }

  get levelProgressStyle() {
    return `width:${Math.round(this.levelProgress)}%;`;
  }

  get xpToNextLevel() {
    return this.gamification.xpForNextLevel - this.gamification.totalXp;
  }

  get isPerfect() {
    return (this.stats.wrongAnswersCount || 0) === 0;
  }

  // Trophy tier based on accuracy
  get trophyTier() {
    if (this.accuracy >= 90) return 'gold';
    if (this.accuracy >= 60) return 'silver';
    return 'bronze';
  }

  get celebrationIntensity() {
    if (this.accuracy >= 90) return 'intense';
    if (this.accuracy >= 60) return 'medium';
    return 'mild';
  }

  <template>
    <div class="exercise-stats__container rounded-large flex flex-col items-center justify-between w-full h-full {{this.animationTheme}} {{this.bgPalette}} celebration-{{this.celebrationIntensity}}">

      {{!-- ===== Animated gradient background ===== --}}
      <div class="exercise-stats__bg-glow" aria-hidden="true"></div>

      {{!-- ===== Ripple waves from center ===== --}}
      <div class="exercise-stats__ripples" aria-hidden="true">
        {{#each this.ripples as |ripple|}}
          <div class="exercise-stats__ripple" style={{htmlSafe ripple.style}}></div>
        {{/each}}
      </div>

      {{!-- ===== Firework bursts ===== --}}
      <div class="exercise-stats__fireworks" aria-hidden="true">
        {{#each this.fireworks as |fw|}}
          <div class="exercise-stats__firework" style={{htmlSafe fw.style}}>
            {{#each fw.particles as |particle|}}
              <div class="exercise-stats__fw-particle" style={{htmlSafe particle.style}}></div>
            {{/each}}
          </div>
        {{/each}}
      </div>

      {{!-- ===== Confetti cannons ===== --}}
      <div class="exercise-stats__confetti" aria-hidden="true">
        {{#each this.confettiPieces as |piece|}}
          <div
            class="exercise-stats__confetti-piece exercise-stats__confetti-piece--{{piece.shape}}"
            style={{htmlSafe piece.style}}
          ></div>
        {{/each}}
      </div>

      {{!-- ===== Shooting stars ===== --}}
      <div class="exercise-stats__shooting-stars" aria-hidden="true">
        {{#each this.shootingStars as |star|}}
          <div class="exercise-stats__shooting-star" style={{htmlSafe star.style}}></div>
        {{/each}}
      </div>

      {{!-- ===== Floating celebration SVG icons ===== --}}
      <div class="exercise-stats__floating-icons" aria-hidden="true">
        {{#each this.floatingIcons as |icon|}}
          <img
            src={{icon.src}}
            alt=""
            class="exercise-stats__floating-icon"
            style={{htmlSafe icon.style}}
          />
        {{/each}}
      </div>

      <div
        data-test-exercise-stats
        ...attributes
        class="justify-evenly flex flex-col flex-1 w-full exercise-stats__content"
      >
        {{!-- ===== Trophy + Accuracy ring (hero visual) ===== --}}
        <div class="exercise-stats__hero">
          {{!-- Trophy icon based on accuracy tier --}}
          <div class="exercise-stats__trophy exercise-stats__trophy--{{this.trophyTier}}">
            {{#if this.isPerfect}}
              <svg viewBox="0 0 64 64" width="40" height="40" class="exercise-stats__trophy-icon">
                <path d="M16 8h32v4H16z" fill="#fbbf24"/>
                <path d="M18 12h28v20c0 8-6 14-14 14s-14-6-14-14V12z" fill="#fbbf24" stroke="#f59e0b" stroke-width="1.5"/>
                <path d="M18 16c-6 0-10 4-10 8s4 8 8 8c2 0 4-1 4-1" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"/>
                <path d="M46 16c6 0 10 4 10 8s-4 8-8 8c-2 0-4-1-4-1" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"/>
                <rect x="26" y="46" width="12" height="4" rx="1" fill="#f59e0b"/>
                <rect x="22" y="50" width="20" height="4" rx="2" fill="#fbbf24" stroke="#f59e0b" stroke-width="1"/>
                <polygon points="32,20 34,26 40,26 35,30 37,36 32,32 27,36 29,30 24,26 30,26" fill="#fff" opacity="0.9"/>
              </svg>
            {{/if}}
          </div>

          <div class="exercise-stats__ring-wrap">
            {{!-- Glow halo --}}
            <div class="exercise-stats__ring-glow exercise-stats__ring-glow--{{this.trophyTier}}"></div>
            {{!-- Orbiting particle --}}
            <div class="exercise-stats__orbit">
              <div class="exercise-stats__orbit-dot"></div>
            </div>
            <svg viewBox="0 0 96 96" class="exercise-stats__accuracy-ring"
            role="img" aria-label="{{t "gamification.accuracy"}} {{this.accuracy}}%">
              <circle cx="48" cy="48" r="40" fill="none" stroke="#e5e7eb" stroke-width="6"/>
              <circle cx="48" cy="48" r="40" fill="none"
                stroke={{this.accuracyRingColor}} stroke-width="6"
                stroke-dasharray={{this.accuracyCircumference}}
                stroke-dashoffset={{this.accuracyDashoffset}}
                stroke-linecap="round" transform="rotate(-90 48 48)"
                class="exercise-stats__accuracy-fill"/>
              {{!-- Sparkles around the ring --}}
              {{#each this.sparkles as |sparkle|}}
                <circle cx={{sparkle.cx}} cy={{sparkle.cy}} r="2"
                  fill="#fbbf24" class="exercise-stats__sparkle"
                  style={{htmlSafe sparkle.style}}/>
              {{/each}}
              {{#if this.isPerfect}}
                <polygon points="48,26 52,38 64,39 55,47 57,59 48,53 39,59 41,47 32,39 44,38"
                  fill={{this.accuracyRingColor}} class="exercise-stats__star"/>
                {{!-- Extra mini stars for perfect --}}
                <polygon points="28,20 29,23 32,23 30,25 31,28 28,26 25,28 26,25 24,23 27,23"
                  fill="#fbbf24" class="exercise-stats__mini-star exercise-stats__mini-star--1"/>
                <polygon points="68,22 69,25 72,25 70,27 71,30 68,28 65,30 66,27 64,25 67,25"
                  fill="#fbbf24" class="exercise-stats__mini-star exercise-stats__mini-star--2"/>
                <polygon points="48,68 49,71 52,71 50,73 51,76 48,74 45,76 46,73 44,71 47,71"
                  fill="#fbbf24" class="exercise-stats__mini-star exercise-stats__mini-star--3"/>
              {{else}}
                <text x="48" y="44" text-anchor="middle" dominant-baseline="central"
                  class="exercise-stats__accuracy-text">{{this.accuracy}}%</text>
                <text x="48" y="60" text-anchor="middle"
                  class="exercise-stats__accuracy-sub">{{t "gamification.accuracy"}}</text>
              {{/if}}
            </svg>
          </div>
          <p class="exercise-stats__message {{this.accuracyClass}}">
            {{t this.growthMessageKey}}
          </p>
        </div>

        {{!-- ===== Stat cards (2×2) ===== --}}
        <div class="exercise-stats__grid">
          <div class="exercise-stats__card exercise-stats__item exercise-stats__card--animated">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon exercise-stats__card-icon--bounce"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2"/><path d="M12 7v5l3 3" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/></svg>
            <span class="exercise-stats__card-value">{{or this.timeStats.min 0}}{{t "statistics.time_min"}} {{or this.timeStats.sec 0}}{{t "statistics.time_sec"}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.time_board"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item exercise-stats__card--animated">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon exercise-stats__card-icon--bounce"><path d="M9 12l2 2 4-4" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="12" r="10" stroke="#22c55e" stroke-width="2"/></svg>
            <span class="exercise-stats__card-value">{{or this.stats.rightAnswersCount 0}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.tasks"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item exercise-stats__card--animated">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon exercise-stats__card-icon--bounce"><path d="M17 1l4 4-4 4" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 11V9a4 4 0 014-4h14M7 23l-4-4 4-4" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 13v2a4 4 0 01-4 4H3" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span class="exercise-stats__card-value">{{or this.stats.repeatsCount 0}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.repeats"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item exercise-stats__card--animated">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon exercise-stats__card-icon--bounce"><circle cx="12" cy="12" r="10" stroke="#ef4444" stroke-width="2"/><path d="M15 9l-6 6M9 9l6 6" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/></svg>
            <span class="exercise-stats__card-value">{{or this.stats.wrongAnswersCount 0}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.wrong_answers"}}</span>
          </div>
        </div>

        {{!-- ===== XP & Level progress ===== --}}
        <div class="exercise-stats__xp-section exercise-stats__item">
          <div class="exercise-stats__xp-header">
            <span class="exercise-stats__xp-earned">+{{this.sessionXp}} XP</span>
            <span class="exercise-stats__xp-level">{{t "gamification.level"}} {{this.currentLevel}}</span>
          </div>
          <div class="exercise-stats__level-bar">
            <div class="exercise-stats__level-bar-fill exercise-stats__level-bar-fill--rainbow" style={{htmlSafe this.levelProgressStyle}}></div>
          </div>
          <span class="exercise-stats__xp-remaining">{{this.xpToNextLevel}} XP {{t "gamification.xp_to_next_level"}}</span>
        </div>

        {{!-- ===== Streak & lifetime stats ===== --}}
        <div class="exercise-stats__footer-stats">
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none" class="exercise-stats__footer-icon"><path d="M10 2C10 2 5 7 5 11a5 5 0 0010 0c0-4-5-9-5-9z" fill="#f97316"/><path d="M10 8c0 0-2 2.5-2 4.5a2 2 0 004 0C12 10.5 10 8 10 8z" fill="#fbbf24"/></svg>
            <span class="exercise-stats__footer-value">{{this.streakStatus}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.streak"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none" class="exercise-stats__footer-icon"><path d="M10 1l2.5 5 5.5 1-4 3.8 1 5.5L10 13.5 4.9 16.3l1-5.5L2 7l5.5-1z" fill="#eab308"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.longestStreak}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.longest_streak"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none" class="exercise-stats__footer-icon"><rect x="2" y="10" width="4" height="8" rx="1" fill="#6366f1"/><rect x="8" y="6" width="4" height="12" rx="1" fill="#818cf8"/><rect x="14" y="2" width="4" height="16" rx="1" fill="#a5b4fc"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.exercisesCompleted}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.total_exercises"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none" class="exercise-stats__footer-icon"><circle cx="10" cy="10" r="8" stroke="#22c55e" stroke-width="2" fill="none"/><path d="M7 10l2 2 4-4" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.perfectExercises}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.perfect_exercises"}}</span>
          </div>
        </div>

        {{!-- ===== Continue ===== --}}
        <div class="exercise-stats__action">
          <UiButton
            data-test-continue
            class="w-auto px-8 exercise-stats__continue-btn"
            @title={{t "statistics.continue"}}
            {{on "click" @onComplete}}
          />
        </div>
      </div>
    </div>
  </template>
}
