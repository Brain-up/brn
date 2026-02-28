import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema, LegacyModeFieldSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { storeFor } from '@warp-drive/core';
import { getOwner } from '@ember/application';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import arrayNext from 'brn/utils/array-next';
import type TasksManagerService from 'brn/services/tasks-manager';
import type StudyingTimerService from 'brn/services/studying-timer';
import type { ExerciseMechanism } from 'brn/utils/exercise-types';
import type AnswerOption from 'brn/utils/answer-option';

/**
 * Helper to look up a service from a record instance.
 */
function getService<T>(record: unknown, serviceName: string): T | null {
  const store = storeFor(record as any, true);
  if (!store) return null;
  const owner = getOwner(store);
  if (!owner) return null;
  return owner.lookup(`service:${serviceName}`) as T;
}

interface ExerciseRef {
  audioFileUrlGenerated: boolean;
  sortedChildren: unknown[] | null;
  wordsColumns: number;
  playWordsCount?: number;
  isStarted: boolean;
  trackTime: (type?: string) => void;
  tasks: unknown[];
  [key: string]: unknown;
}

/**
 * Base fields shared by all Task schemas.
 * Each task subtype schema should spread these fields into its own fields array.
 */
export const BASE_TASK_FIELDS: LegacyModeFieldSchema[] = [
  { kind: 'attribute', name: 'name', type: 'string' },
  { kind: 'attribute', name: 'exerciseType', type: 'string' },
  { kind: 'attribute', name: 'exerciseMechanism', type: 'string' },
  { kind: 'attribute', name: 'order', type: 'number' },
  { kind: 'attribute', name: 'repetitionCount', type: 'number' },
  { kind: 'attribute', name: 'shouldBeWithPictures', type: 'boolean' },
  { kind: 'attribute', name: 'answerOptions' },
  { kind: 'attribute', name: 'normalizedAnswerOptions' },
  { kind: 'attribute', name: 'correctAnswer' },
  {
    kind: 'belongsTo',
    name: 'exercise',
    type: 'exercise',
    options: { async: false, inverse: 'tasks', as: 'task' },
  },
] as LegacyModeFieldSchema[];

// ---- Base Task Schema ----

export const TaskSchema: LegacyResourceSchema = withDefaults({
  type: 'task',
  fields: [...BASE_TASK_FIELDS],
  objectExtensions: ['task-ext'],
}) as LegacyResourceSchema;

/**
 * Shared @local fields required by all task schemas.
 * These are mutable tracked properties not backed by API attributes.
 */
export const LOCAL_TASK_FIELDS = [
  {
    kind: '@local',
    name: 'isManuallyCompleted',
    type: 'boolean',
    options: { defaultValue: false },
  },
  {
    kind: '@local',
    name: '_completedInCurrentCycle',
    type: 'boolean',
    options: { defaultValue: false },
  },
  {
    kind: '@local',
    name: 'nextAttempt',
    type: 'boolean',
    options: { defaultValue: false },
  },
  {
    kind: '@local',
    name: 'available',
    type: 'boolean',
    options: { defaultValue: false },
  },
] as any[];

// Add tracked local fields
TaskSchema.fields.push(...LOCAL_TASK_FIELDS);

/**
 * The shared Task extension containing CompletionDependent logic
 * and Task-specific methods. Used by all task types.
 */
export const TaskExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-ext',
  features: {
    get sortChildrenBy() {
      return 'order';
    },

    get usePreGeneratedAudio(): boolean {
      const self = this as unknown as { exercise: ExerciseRef };
      return self.exercise.audioFileUrlGenerated;
    },

    get children(): unknown[] {
      return [];
    },

    get sortedChildren(): unknown[] | null {
      return null;
    },

    get parent() {
      const self = this as unknown as { exercise: ExerciseRef };
      return self.exercise;
    },

    set parent(value: unknown) {
      const self = this as unknown as { exercise: unknown };
      self.exercise = value;
    },

    get pauseExecution(): boolean {
      const self = this as unknown as Record<string, unknown>;
      const studyingTimer = getService<StudyingTimerService>(self, 'studying-timer');
      return studyingTimer?.isPaused ?? false;
    },

    get isCompleted(): boolean {
      const self = this as unknown as Record<string, unknown>;
      const tasksManager = getService<TasksManagerService>(self, 'tasks-manager');
      if (!tasksManager) return false;
      return tasksManager.isCompleted(self as any);
    },

    get completedInCurrentCycle(): boolean {
      const self = this as unknown as { _completedInCurrentCycle: boolean };
      const tasksManager = getService<TasksManagerService>(self, 'tasks-manager');
      return (
        self._completedInCurrentCycle ||
        (tasksManager?.isCompletedInCurrentCycle(self as any) ?? false)
      );
    },

    set completedInCurrentCycle(value: boolean) {
      const self = this as unknown as { _completedInCurrentCycle: boolean };
      self._completedInCurrentCycle = value;
    },

    get nextTask(): unknown {
      const self = this as unknown as { exercise: ExerciseRef };
      return arrayNext(self, self.exercise.sortedChildren);
    },

    get isLastTask(): boolean {
      const self = this as unknown as { nextTask: unknown };
      return self.nextTask == null;
    },

    get savePassed() {
      const self = this as unknown as Record<string, unknown>;
      return () => {
        const tasksManager = getService<TasksManagerService>(self, 'tasks-manager');
        if (!tasksManager) return;
        return tasksManager.saveAsCompleted(self as any);
      };
    },

    // CompletionDependent properties

    get allSiblings(): unknown[] {
      const self = this as unknown as { parent: { sortedChildren: unknown[] | null } };
      return self.parent?.sortedChildren || [];
    },

    get previousSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return arrayPreviousItems(self, self.allSiblings);
    },

    get nextSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return self.allSiblings.slice(self.allSiblings.indexOf(self) + 1);
    },

    get isFirst(): boolean {
      const self = this as unknown as { previousSiblings: unknown[] };
      return self.previousSiblings.length === 0;
    },

    get canInteract(): boolean {
      const self = this as unknown as {
        available?: boolean;
        previousSiblings: unknown[];
      };
      if (self.available) {
        return true;
      }
      return (
        self.previousSiblings.length === 0 ||
        self.previousSiblings.every((sibling: any) => sibling.isCompleted)
      );
    },

  },
};

export type TaskBase = WithLegacy<{
  name: string;
  exerciseType: string;
  exerciseMechanism: ExerciseMechanism;
  order: number;
  repetitionCount: number;
  shouldBeWithPictures: boolean;
  answerOptions: any;
  normalizedAnswerOptions: AnswerOption[];
  exercise: ExerciseRef;
  isManuallyCompleted: boolean;
  _completedInCurrentCycle: boolean;
  nextAttempt: boolean;
  available: boolean;
  sortChildrenBy: string;
  usePreGeneratedAudio: boolean;
  children: unknown[];
  sortedChildren: unknown[] | null;
  parent: ExerciseRef;
  pauseExecution: boolean;
  isCompleted: boolean;
  completedInCurrentCycle: boolean;
  nextTask: unknown;
  isLastTask: boolean;
  savePassed: () => void;
  allSiblings: unknown[];
  previousSiblings: unknown[];
  nextSiblings: unknown[];
  isFirst: boolean;
  canInteract: boolean;
  [Type]: 'task' | 'task/signal' | 'task/single-simple-words' | 'task/words-sequences';
}>;
