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

interface ExerciseStatsSignature {
  Args: {
  stats: IStatsExerciseStats;
  onComplete: () => void;
  };
  Element: HTMLElement;
}

export default class ExerciseStatsComponent extends Component<ExerciseStatsSignature> {
  @service('gamification') declare gamification: GamificationService;

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

  // SVG accuracy ring: radius 40, circumference ~251.3
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

  <template>
    <div class="exercise-stats__container rounded-large flex flex-col items-center justify-between w-full h-full">
      <div
        data-test-exercise-stats
        ...attributes
        class="justify-evenly flex flex-col flex-1 w-full"
      >
        {{!-- ===== Accuracy ring (hero visual) ===== --}}
        <div class="exercise-stats__hero">
          <div class="exercise-stats__ring-wrap">
            <svg viewBox="0 0 96 96" class="exercise-stats__accuracy-ring"
            role="img" aria-label="{{t "gamification.accuracy"}} {{this.accuracy}}%">
              <circle cx="48" cy="48" r="40" fill="none" stroke="#e5e7eb" stroke-width="6"/>
              <circle cx="48" cy="48" r="40" fill="none"
                stroke={{this.accuracyRingColor}} stroke-width="6"
                stroke-dasharray={{this.accuracyCircumference}}
                stroke-dashoffset={{this.accuracyDashoffset}}
                stroke-linecap="round" transform="rotate(-90 48 48)"
                class="exercise-stats__accuracy-fill"/>
              {{#if this.isPerfect}}
                <polygon points="48,26 52,38 64,39 55,47 57,59 48,53 39,59 41,47 32,39 44,38"
                  fill={{this.accuracyRingColor}} class="exercise-stats__star"/>
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
          <div class="exercise-stats__card exercise-stats__item">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2"/><path d="M12 7v5l3 3" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/></svg>
            <span class="exercise-stats__card-value">{{or this.timeStats.min 0}}{{t "statistics.time_min"}} {{or this.timeStats.sec 0}}{{t "statistics.time_sec"}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.time_board"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon"><path d="M9 12l2 2 4-4" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="12" r="10" stroke="#22c55e" stroke-width="2"/></svg>
            <span class="exercise-stats__card-value">{{or this.stats.rightAnswersCount 0}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.tasks"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon"><path d="M17 1l4 4-4 4" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 11V9a4 4 0 014-4h14M7 23l-4-4 4-4" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 13v2a4 4 0 01-4 4H3" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span class="exercise-stats__card-value">{{or this.stats.repeatsCount 0}}</span>
            <span class="exercise-stats__card-label">{{t "statistics.repeats"}}</span>
          </div>

          <div class="exercise-stats__card exercise-stats__item">
            <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="exercise-stats__card-icon"><circle cx="12" cy="12" r="10" stroke="#ef4444" stroke-width="2"/><path d="M15 9l-6 6M9 9l6 6" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/></svg>
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
            <div class="exercise-stats__level-bar-fill" style={{htmlSafe this.levelProgressStyle}}></div>
          </div>
          <span class="exercise-stats__xp-remaining">{{this.xpToNextLevel}} XP {{t "gamification.xp_to_next_level"}}</span>
        </div>

        {{!-- ===== Streak & lifetime stats ===== --}}
        <div class="exercise-stats__footer-stats">
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none"><path d="M10 2C10 2 5 7 5 11a5 5 0 0010 0c0-4-5-9-5-9z" fill="#f97316"/><path d="M10 8c0 0-2 2.5-2 4.5a2 2 0 004 0C12 10.5 10 8 10 8z" fill="#fbbf24"/></svg>
            <span class="exercise-stats__footer-value">{{this.streakStatus}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.streak"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none"><path d="M10 1l2.5 5 5.5 1-4 3.8 1 5.5L10 13.5 4.9 16.3l1-5.5L2 7l5.5-1z" fill="#eab308"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.longestStreak}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.longest_streak"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none"><rect x="2" y="10" width="4" height="8" rx="1" fill="#6366f1"/><rect x="8" y="6" width="4" height="12" rx="1" fill="#818cf8"/><rect x="14" y="2" width="4" height="16" rx="1" fill="#a5b4fc"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.exercisesCompleted}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.total_exercises"}}</span>
          </div>
          <div class="exercise-stats__footer-item exercise-stats__item">
            <svg viewBox="0 0 20 20" width="16" height="16" fill="none"><circle cx="10" cy="10" r="8" stroke="#22c55e" stroke-width="2" fill="none"/><path d="M7 10l2 2 4-4" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span class="exercise-stats__footer-value">{{this.gamification.perfectExercises}}</span>
            <span class="exercise-stats__footer-label">{{t "gamification.perfect_exercises"}}</span>
          </div>
        </div>

        {{!-- ===== Continue ===== --}}
        <div class="exercise-stats__action">
          <UiButton
            data-test-continue
            class="w-auto px-8"
            @title={{t "statistics.continue"}}
            {{on "click" @onComplete}}
          />
        </div>
      </div>
    </div>
  </template>
}
