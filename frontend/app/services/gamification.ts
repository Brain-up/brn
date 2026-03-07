import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

const STORAGE_KEY = 'brn-gamification';

interface GamificationState {
  totalXp: number;
  currentStreak: number;
  longestStreak: number;
  lastActiveDate: string | null;
  exercisesCompleted: number;
  perfectExercises: number;
  badges: Record<string, string | null>;
}

const DEFAULT_STATE: GamificationState = {
  totalXp: 0,
  currentStreak: 0,
  longestStreak: 0,
  lastActiveDate: null,
  exercisesCompleted: 0,
  perfectExercises: 0,
  badges: {},
};

const XP_RIGHT_ANSWER = 10;
const XP_EXERCISE_COMPLETION = 25;
const XP_PERFECT_BONUS = 15;
const MAX_STREAK_BONUS = 50;

export default class GamificationService extends Service {
  @tracked private _state: GamificationState = DEFAULT_STATE;
  @tracked sessionXp = 0;
  @tracked lastXpGain = 0;
  @tracked showXpPopup = false;

  private _popupTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(properties?: ConstructorParameters<typeof Service>[0]) {
    super(properties);
    this._state = this.loadState();
    this.refreshStreak();
  }

  // ---------------------------------------------------------------------------
  // Computed getters
  // ---------------------------------------------------------------------------

  get totalXp(): number {
    return this._state.totalXp;
  }

  get currentStreak(): number {
    return this._state.currentStreak;
  }

  get longestStreak(): number {
    return this._state.longestStreak;
  }

  get lastActiveDate(): string | null {
    return this._state.lastActiveDate;
  }

  get exercisesCompleted(): number {
    return this._state.exercisesCompleted;
  }

  get perfectExercises(): number {
    return this._state.perfectExercises;
  }

  get badges(): Record<string, string | null> {
    return this._state.badges;
  }

  /**
   * Level N requires cumulative sum(i * 50 for i = 2..N) XP.
   * Level 1 requires 0 XP. Level 2 requires 100 XP. Level 3 requires 100 + 150 = 250 XP, etc.
   */
  get level(): number {
    let cumulative = 0;
    let n = 1;
    while (true) {
      const nextLevelCost = (n + 1) * 50;
      if (cumulative + nextLevelCost > this.totalXp) {
        return n;
      }
      cumulative += nextLevelCost;
      n++;
    }
  }

  /**
   * Cumulative XP required to reach the current level.
   */
  get xpForCurrentLevel(): number {
    let cumulative = 0;
    for (let i = 2; i <= this.level; i++) {
      cumulative += i * 50;
    }
    return cumulative;
  }

  /**
   * Cumulative XP required to reach the next level.
   */
  get xpForNextLevel(): number {
    let cumulative = 0;
    for (let i = 2; i <= this.level + 1; i++) {
      cumulative += i * 50;
    }
    return cumulative;
  }

  get earnedBadges(): Record<string, string> {
    const result: Record<string, string> = {};
    for (const [key, value] of Object.entries(this._state.badges)) {
      if (value !== null) {
        result[key] = value;
      }
    }
    return result;
  }

  get streakBonusXp(): number {
    return Math.min(this._state.currentStreak * 5, MAX_STREAK_BONUS);
  }

  /**
   * Percentage progress toward the next level (0-100).
   */
  get xpProgress(): number {
    const levelRange = this.xpForNextLevel - this.xpForCurrentLevel;
    if (levelRange <= 0) {
      return 0;
    }
    const xpIntoLevel = this.totalXp - this.xpForCurrentLevel;
    return Math.min(100, Math.max(0, (xpIntoLevel / levelRange) * 100));
  }

  // ---------------------------------------------------------------------------
  // Public methods
  // ---------------------------------------------------------------------------

  addRightAnswerXp(): void {
    this.addXp(XP_RIGHT_ANSWER);
  }

  completeExercise({
    wrongAnswersCount,
    countedSeconds,
  }: {
    wrongAnswersCount: number;
    countedSeconds: number;
  }): void {
    let xpGained = XP_EXERCISE_COMPLETION;

    // Perfect bonus
    const isPerfect = wrongAnswersCount === 0;
    if (isPerfect) {
      xpGained += XP_PERFECT_BONUS;
    }

    // Streak bonus (capped at MAX_STREAK_BONUS)
    const streakBonus = Math.min(this._state.currentStreak * 5, MAX_STREAK_BONUS);
    xpGained += streakBonus;

    // Update exercises completed
    const newState = { ...this._state };
    newState.exercisesCompleted = this._state.exercisesCompleted + 1;

    if (isPerfect) {
      newState.perfectExercises = this._state.perfectExercises + 1;
    }

    // Update streak
    this.updateStreak(newState);

    // Apply XP
    newState.totalXp = this._state.totalXp + xpGained;

    // Check badges
    const now = new Date().toISOString();
    const badges = { ...newState.badges };

    // first_exercise - complete 1 exercise
    if (!badges['first_exercise'] && newState.exercisesCompleted >= 1) {
      badges['first_exercise'] = now;
    }

    // streak_7 - 7 day streak
    if (!badges['streak_7'] && newState.currentStreak >= 7) {
      badges['streak_7'] = now;
    }

    // perfect_score - complete with 0 wrong answers
    if (!badges['perfect_score'] && isPerfect) {
      badges['perfect_score'] = now;
    }

    // speed_listener - complete in under 60 seconds
    if (!badges['speed_listener'] && countedSeconds < 60) {
      badges['speed_listener'] = now;
    }

    // exercises_10 - complete 10 exercises
    if (!badges['exercises_10'] && newState.exercisesCompleted >= 10) {
      badges['exercises_10'] = now;
    }

    // exercises_50 - complete 50 exercises
    if (!badges['exercises_50'] && newState.exercisesCompleted >= 50) {
      badges['exercises_50'] = now;
    }

    // consistent_30 - 30 day streak
    if (!badges['consistent_30'] && newState.currentStreak >= 30) {
      badges['consistent_30'] = now;
    }

    // sound_master - reach level 10
    // Compute level from the new totalXp
    if (!badges['sound_master'] && this.computeLevel(newState.totalXp) >= 10) {
      badges['sound_master'] = now;
    }

    // Placeholder badges remain null
    if (badges['series_explorer'] === undefined) {
      badges['series_explorer'] = null;
    }
    if (badges['comeback_kid'] === undefined) {
      badges['comeback_kid'] = null;
    }

    newState.badges = badges;

    // Update transient state
    this.sessionXp = this.sessionXp + xpGained;
    this.lastXpGain = xpGained;
    this.schedulePopupDismiss();

    // Persist
    this._state = newState;
    this.saveState(newState);
  }

  resetSession(): void {
    this.sessionXp = 0;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  private addXp(amount: number): void {
    const newState = { ...this._state };
    newState.totalXp = this._state.totalXp + amount;
    this.sessionXp = this.sessionXp + amount;
    this.lastXpGain = amount;
    this.schedulePopupDismiss();
    this._state = newState;
    this.saveState(newState);
  }

  private refreshStreak(): void {
    const today = this.todayDateString();
    const lastActive = this._state.lastActiveDate;
    if (lastActive && lastActive !== today && lastActive !== this.yesterdayDateString()) {
      const newState = { ...this._state };
      newState.currentStreak = 0;
      this._state = newState;
      this.saveState(newState);
    }
  }

  private schedulePopupDismiss(): void {
    this.showXpPopup = true;
    if (this._popupTimer) {
      clearTimeout(this._popupTimer);
    }
    this._popupTimer = setTimeout(() => {
      this.showXpPopup = false;
      this._popupTimer = null;
    }, 2000);
  }

  private updateStreak(state: GamificationState): void {
    const today = this.todayDateString();
    const lastActive = state.lastActiveDate;

    if (lastActive === today) {
      // Already active today, no streak change
      return;
    }

    if (lastActive === this.yesterdayDateString()) {
      // Consecutive day, increment streak
      state.currentStreak = state.currentStreak + 1;
    } else {
      // Gap in activity, reset streak
      state.currentStreak = 1;
    }

    if (state.currentStreak > state.longestStreak) {
      state.longestStreak = state.currentStreak;
    }

    state.lastActiveDate = today;
  }

  private computeLevel(totalXp: number): number {
    let cumulative = 0;
    let n = 1;
    while (true) {
      const nextLevelCost = (n + 1) * 50;
      if (cumulative + nextLevelCost > totalXp) {
        return n;
      }
      cumulative += nextLevelCost;
      n++;
    }
  }

  private todayDateString(): string {
    return this.normalizeDateString(new Date());
  }

  private yesterdayDateString(): string {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    return this.normalizeDateString(yesterday);
  }

  private normalizeDateString(date: Date): string {
    return date.toISOString().split('T')[0]!;
  }

  private loadState(): GamificationState {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return { ...DEFAULT_STATE, badges: { ...DEFAULT_STATE.badges } };
      }
      const parsed = JSON.parse(raw) as Partial<GamificationState>;
      return {
        totalXp:
          typeof parsed.totalXp === 'number' ? parsed.totalXp : DEFAULT_STATE.totalXp,
        currentStreak:
          typeof parsed.currentStreak === 'number'
            ? parsed.currentStreak
            : DEFAULT_STATE.currentStreak,
        longestStreak:
          typeof parsed.longestStreak === 'number'
            ? parsed.longestStreak
            : DEFAULT_STATE.longestStreak,
        lastActiveDate:
          typeof parsed.lastActiveDate === 'string'
            ? parsed.lastActiveDate
            : DEFAULT_STATE.lastActiveDate,
        exercisesCompleted:
          typeof parsed.exercisesCompleted === 'number'
            ? parsed.exercisesCompleted
            : DEFAULT_STATE.exercisesCompleted,
        perfectExercises:
          typeof parsed.perfectExercises === 'number'
            ? parsed.perfectExercises
            : DEFAULT_STATE.perfectExercises,
        badges:
          parsed.badges && typeof parsed.badges === 'object'
            ? { ...parsed.badges }
            : { ...DEFAULT_STATE.badges },
      };
    } catch {
      return { ...DEFAULT_STATE, badges: { ...DEFAULT_STATE.badges } };
    }
  }

  private saveState(state: GamificationState): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch {
      // Storage full or unavailable; silently ignore
    }
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    gamification: GamificationService;
  }
}
