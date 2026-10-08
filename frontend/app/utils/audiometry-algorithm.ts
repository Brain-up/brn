/**
 * Modified Hughson-Westlake adaptive threshold algorithm for pure-tone audiometry.
 *
 * Procedure:
 * 1. Present tone at INITIAL_DB (40 dB HL).
 * 2. If heard → decrease by STEP_DOWN_DB (10 dB).
 * 3. If not heard → increase by STEP_UP_DB (5 dB).
 * 4. Track direction changes (reversals).
 * 5. Threshold = lowest dB heard on REQUIRED_ASCENDING out of 3 ascending runs.
 * 6. Complete when threshold found, MAX_REVERSALS reached, or dB bounds hit repeatedly.
 */

export const INITIAL_DB = 40;
export const MIN_DB = 0;
export const MAX_DB = 90;
export const STEP_DOWN_DB = 10;
export const STEP_UP_DB = 5;
const REQUIRED_ASCENDING = 2;
const MAX_REVERSALS = 8;
const MAX_TRIALS = 30;

export interface TrialResult {
  dB: number;
  heard: boolean;
}

export interface ThresholdState {
  currentDB: number;
  trials: TrialResult[];
  reversals: number;
  lastDirection: 'up' | 'down' | null;
  ascendingHeard: Map<number, number>; // dB level → count of ascending "heard"
  isComplete: boolean;
  threshold: number | null;
}

export function createThresholdState(): ThresholdState {
  return {
    currentDB: INITIAL_DB,
    trials: [],
    reversals: 0,
    lastDirection: null,
    ascendingHeard: new Map(),
    isComplete: false,
    threshold: null,
  };
}

export function processResponse(
  state: ThresholdState,
  heard: boolean,
): ThresholdState {
  if (state.isComplete) return state;

  const trial: TrialResult = { dB: state.currentDB, heard };
  const trials = [...state.trials, trial];

  let direction: 'up' | 'down';
  let nextDB: number;

  if (heard) {
    direction = 'down';
    nextDB = state.currentDB - STEP_DOWN_DB;
  } else {
    direction = 'up';
    nextDB = state.currentDB + STEP_UP_DB;
  }

  // Clamp to bounds
  nextDB = Math.max(MIN_DB, Math.min(MAX_DB, nextDB));

  // Count reversals (direction change)
  let reversals = state.reversals;
  if (state.lastDirection !== null && direction !== state.lastDirection) {
    reversals++;
  }

  // Track ascending "heard" responses:
  // An ascending response is when direction was "up" (previous was not-heard)
  // and now the user heard it — i.e., we went up and they heard it.
  const ascendingHeard = new Map(state.ascendingHeard);
  if (heard && state.lastDirection === 'up') {
    const count = ascendingHeard.get(state.currentDB) ?? 0;
    ascendingHeard.set(state.currentDB, count + 1);
  }

  // Check threshold: lowest dB with REQUIRED_ASCENDING ascending "heard" responses
  let threshold: number | null = null;
  let isComplete = false;

  for (const [dB, count] of ascendingHeard) {
    if (count >= REQUIRED_ASCENDING) {
      if (threshold === null || dB < threshold) {
        threshold = dB;
      }
    }
  }

  if (threshold !== null) {
    isComplete = true;
  }

  // Safety stops
  if (reversals >= MAX_REVERSALS && !isComplete) {
    isComplete = true;
    // Best estimate: find the lowest dB with at least 1 ascending heard
    for (const [dB, count] of ascendingHeard) {
      if (count >= 1) {
        if (threshold === null || dB < threshold) {
          threshold = dB;
        }
      }
    }
    // Fallback: use most recent heard level
    if (threshold === null) {
      const lastHeard = [...trials].reverse().find((t) => t.heard);
      threshold = lastHeard?.dB ?? MAX_DB;
    }
  }

  if (trials.length >= MAX_TRIALS && !isComplete) {
    isComplete = true;
    if (threshold === null) {
      const lastHeard = [...trials].reverse().find((t) => t.heard);
      threshold = lastHeard?.dB ?? MAX_DB;
    }
  }

  // Edge case: hit MAX_DB and still not heard
  if (nextDB >= MAX_DB && !heard && !isComplete) {
    const maxHits = trials.filter((t) => t.dB >= MAX_DB && !t.heard).length;
    if (maxHits >= 3) {
      isComplete = true;
      threshold = MAX_DB;
    }
  }

  // Edge case: hit MIN_DB and heard
  if (nextDB <= MIN_DB && heard && !isComplete) {
    const minHeard = trials.filter((t) => t.dB <= MIN_DB + STEP_UP_DB && t.heard).length;
    if (minHeard >= 2) {
      isComplete = true;
      threshold = MIN_DB;
    }
  }

  return {
    currentDB: nextDB,
    trials,
    reversals,
    lastDirection: direction,
    ascendingHeard,
    isComplete,
    threshold,
  };
}

export function getNextDB(state: ThresholdState): number {
  return state.currentDB;
}

/**
 * Map audiometric dB HL to Tone.js dB scale.
 * Tone.js uses 0 dB = max digital output.
 * We map: MAX_DB HL → 0 dB (Tone.js full volume), 0 dB HL → -MAX_DB dB.
 */
export function dBHLtoToneDB(dBHL: number): number {
  return dBHL - MAX_DB;
}

/**
 * Calculate masking noise level: signal - 30 dB.
 * Returns 0 when the signal is too low to need masking (≤ 30 dB HL).
 */
export function getMaskingLevel(signalDB: number): number {
  if (signalDB <= 30) return 0;
  return signalDB - 30;
}

/**
 * WHO hearing classification based on average threshold across speech frequencies.
 * Pure-tone average (PTA) over 500, 1000, 2000, 4000 Hz.
 */
export type HearingClassification =
  | 'normal'
  | 'mild_loss'
  | 'moderate_loss'
  | 'moderately_severe_loss'
  | 'severe_loss'
  | 'profound_loss';

export function classifyHearing(avgThresholdDB: number): HearingClassification {
  if (avgThresholdDB <= 20) return 'normal';
  if (avgThresholdDB <= 40) return 'mild_loss';
  if (avgThresholdDB <= 55) return 'moderate_loss';
  if (avgThresholdDB <= 70) return 'moderately_severe_loss';
  if (avgThresholdDB <= 90) return 'severe_loss';
  return 'profound_loss';
}

/**
 * Calculate pure-tone average from thresholds at speech frequencies.
 * Uses frequencies: 500, 1000, 2000, 4000 Hz (WHO standard).
 * Returns average of available speech frequencies, or null if none available.
 */
const SPEECH_FREQUENCIES = [500, 1000, 2000, 4000];

export function calculatePTA(thresholds: Record<number, number>): number | null {
  const values = SPEECH_FREQUENCIES
    .filter((f) => thresholds[f] !== undefined)
    .map((f) => thresholds[f]!);
  if (values.length === 0) return null;
  return Math.round(values.reduce((sum, v) => sum + v, 0) / values.length);
}
