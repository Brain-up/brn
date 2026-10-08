/**
 * Shared types for UserWeeklyStatistics / UserYearlyStatistics.
 * Extracted from the old model class so they can be imported
 * by both schema definitions and consuming components.
 */

export enum PROGRESS {
  /* eslint-disable no-unused-vars */
  BAD = 'BAD',
  GOOD = 'GOOD',
  GREAT = 'GREAT',
}

export type UserExercisingProgressStatusType =
  | PROGRESS.BAD
  | PROGRESS.GOOD
  | PROGRESS.GREAT;
