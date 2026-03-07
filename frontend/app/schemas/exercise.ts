import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema, LegacyModeFieldSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { getService } from 'brn/utils/schema-helpers';
import { sortByKey } from 'brn/utils/sort-by-key';
import type TasksManagerService from 'brn/services/tasks-manager';
import type NetworkService from 'brn/services/network';
import type { IStatsExerciseStats } from 'brn/services/stats';

export interface IStatsObject {
  countedSeconds: number;
  endTime: Date;
  exerciseId: '110';
  listeningsCount: number;
  repetitionIndex: number;
  rightAnswersCount: number;
  rightAnswersIndex: number;
  startTime: Date;
  tasksCount: number;
}

export const ExerciseSchema: LegacyResourceSchema = withDefaults({
  type: 'exercise',
  fields: [
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'available', type: 'boolean' },
    { kind: 'attribute', name: 'playWordsCount', type: 'number' },
    { kind: 'attribute', name: 'wordsColumns', type: 'number' },
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'level', type: 'number' },
    { kind: 'attribute', name: 'pictureUrl', type: 'string' },
    { kind: 'attribute', name: 'order', type: 'number' },
    { kind: 'attribute', name: 'exerciseType', type: 'string' },
    { kind: 'attribute', name: 'startTime' },
    { kind: 'attribute', name: 'endTime' },
    { kind: 'attribute', name: 'noise' },
    { kind: 'attribute', name: 'isAudioFileUrlGenerated', type: 'boolean' },
    { kind: 'attribute', name: 'exerciseMechanism', type: 'string' },
    { kind: 'attribute', name: 'template', type: 'string' },
    { kind: 'attribute', name: 'active', type: 'boolean' },
    { kind: 'attribute', name: 'changedBy', type: 'string' },
    { kind: 'attribute', name: 'changedWhen', type: 'string' },
    {
      kind: 'belongsTo',
      name: 'series',
      type: 'series',
      options: { async: false, inverse: 'exercises' },
    },
    {
      kind: 'hasMany',
      name: 'signals',
      type: 'signal',
      options: { async: false, inverse: null },
    },
    {
      kind: 'hasMany',
      name: 'tasks',
      type: 'task',
      options: { async: false, inverse: 'exercise', polymorphic: true },
    },
    {
      kind: 'belongsTo',
      name: 'parent',
      type: 'subgroup',
      options: { async: false, inverse: 'exercises' },
    },
  ],
  objectExtensions: ['exercise-ext'],
}) as LegacyResourceSchema;

// Add isManuallyCompleted as a @local field (tracked mutable state, not persisted)
ExerciseSchema.fields.push({
  kind: '@local',
  name: 'isManuallyCompleted',
  type: 'boolean',
  options: { defaultValue: false },
} as LegacyModeFieldSchema);

interface TaskLike {
  id?: string | null;
  isCompleted?: boolean;
  completedInCurrentCycle?: boolean;
  [key: string]: unknown;
}

interface SeriesLike {
  id?: string | null;
  groupedByNameExercises?: Record<string, unknown[]>;
  sortedExercises?: unknown[];
  [key: string]: unknown;
}

/**
 * Minimal interface for Subgroup properties accessed through Exercise.parent.
 * Avoids circular dependency with the Subgroup schema.
 */
interface SubgroupRef {
  id?: string | null;
  exercises: { available: boolean; [key: string]: unknown }[];
  [key: string]: unknown;
}

interface ExerciseSelf {
  id?: string | null;
  name: string;
  available: boolean;
  startTime: Date;
  endTime: Date;
  noise?: { level?: number; url?: string };
  tasks: TaskLike[];
  series: SeriesLike;
  parent: SubgroupRef;
  isManuallyCompleted: boolean;
  isFirst: boolean;
  canInteract: boolean;
  sortChildrenBy: string;
  [key: string]: unknown;
}

export const ExerciseExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'exercise-ext',
  features: {
    /**
     * Ember's <LinkTo> component accesses `isQueryParams` to check if a model
     * is actually a QueryParams object. SchemaRecord's strict proxy throws
     * on unknown fields, so we expose this to prevent that error.
     */
    get isQueryParams() {
      return undefined;
    },

    get sortChildrenBy() {
      return 'order';
    },

    get children() {
      const self = this as unknown as ExerciseSelf;
      return self.tasks || [];
    },

    get sortedChildren(): unknown[] | null {
      const self = this as unknown as ExerciseSelf;
      const children = self.tasks;
      if (!children) return null;
      return sortByKey(Array.from(children).filter(Boolean), self.sortChildrenBy);
    },

    get sortedTasks() {
      const self = this as unknown as { sortedChildren: unknown[] | null };
      return self.sortedChildren;
    },

    get noiseLevel(): number {
      const self = this as unknown as ExerciseSelf;
      return self.noise?.level || 0;
    },

    get noiseUrl(): string | null {
      const self = this as unknown as ExerciseSelf;
      return self.noise?.url || null;
    },

    get isCompleted(): boolean {
      const self = this as unknown as ExerciseSelf;
      if (self.isManuallyCompleted) {
        return true;
      }
      const tasksManager = getService<TasksManagerService>(self, 'tasks-manager');
      if (!tasksManager) return false;
      const tasksIds: string[] = (self.tasks || []).map((t: TaskLike) => t.id).filter(Boolean) as string[];
      const completedTaskIds = tasksManager.completedTasks.map(
        (t: { id: string }) => t.id,
      );
      const tasksCompleted = tasksIds.every((taskId: string) =>
        completedTaskIds.includes(taskId),
      );
      return (
        (!tasksIds.length && (self.isFirst || self.canInteract)) ||
        tasksCompleted
      );
    },

    get previousSiblings(): unknown[] {
      const self = this as unknown as ExerciseSelf;
      const siblings = self.series?.groupedByNameExercises?.[self.name];
      if (!siblings) return [];
      return arrayPreviousItems(self, siblings);
    },

    get siblingExercises(): unknown[] {
      const self = this as unknown as ExerciseSelf;
      return self.series?.sortedExercises || [];
    },

    get nextSiblings(): unknown[] {
      const self = this as unknown as { siblingExercises: unknown[] };
      return self.siblingExercises.slice(
        self.siblingExercises.indexOf(self) + 1,
      );
    },

    get allSiblings(): unknown[] {
      const self = this as unknown as { siblingExercises: unknown[] };
      return self.siblingExercises;
    },

    get isFirst(): boolean {
      const self = this as unknown as { previousSiblings: unknown[] };
      return self.previousSiblings.length === 0;
    },

    get canInteract(): boolean {
      const self = this as unknown as ExerciseSelf;
      if (self.available) {
        return true;
      }
      const previousSiblings = (
        self as unknown as { previousSiblings: unknown[] }
      ).previousSiblings;
      return (
        previousSiblings.length === 0 ||
        previousSiblings.every((sibling: unknown) => (sibling as { isCompleted: boolean }).isCompleted)
      );
    },

    get isStarted(): boolean {
      const self = this as unknown as ExerciseSelf;
      return !!(self.startTime && !self.endTime);
    },

    get stats(): { startTime: Date; endTime: Date; exerciseId: string | null | undefined } {
      const self = this as unknown as ExerciseSelf;
      return {
        startTime: self.startTime,
        endTime: self.endTime,
        exerciseId: self.id,
      };
    },

    trackTime(type = 'start') {
      const self = this as unknown as ExerciseSelf;
      if (type === 'start') {
        self.startTime = new Date();
      } else if (type === 'end') {
        self.endTime = new Date();
      }
    },

    calcStats(data: IStatsExerciseStats | undefined): IStatsObject {
      if (!data) {
        throw new Error('unable calculate exercise stats');
      }
      const self = this as unknown as { stats: IStatsObject };
      const { stats } = self;
      stats.tasksCount = data.rightAnswersCount - data.repeatsCount;
      stats.rightAnswersCount = data.rightAnswersCount;
      stats.listeningsCount = data.playsCount;
      stats.countedSeconds = data.countedSeconds;
      stats.repetitionIndex = (data.repeatsCount / stats.tasksCount) * 100;
      if (isNaN(stats.repetitionIndex)) {
        stats.repetitionIndex = 0;
      } else {
        stats.repetitionIndex = Number(stats.repetitionIndex.toFixed(2));
      }
      stats.rightAnswersIndex =
        ((data.rightAnswersCount - data.repeatsCount) / stats.tasksCount) * 100;
      if (isNaN(stats.rightAnswersIndex)) {
        stats.rightAnswersIndex = 0;
      } else {
        stats.rightAnswersIndex =
          Number(stats.rightAnswersIndex.toFixed(2)) - stats.repetitionIndex;
      }
      return stats;
    },

    async postHistory(data: IStatsExerciseStats) {
      const self = this as unknown as ExerciseSelf & {
        calcStats: (d: IStatsExerciseStats) => IStatsObject;
      };
      const stats: IStatsObject = self.calcStats(data);
      const network = getService<NetworkService>(self, 'network');
      if (!network) return;
      const newStats = {
        endTime: stats.endTime,
        startTime: stats.startTime,
        executionSeconds: stats.countedSeconds,
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        exerciseId: parseInt(self.id!, 10),
        replaysCount: data.repeatsCount,
        wrongAnswers: data.wrongAnswersCount,
        tasksCount: data.rightAnswersCount,
      };
      await network.postRequest('study-history', newStats);
    },
  },
};

export type Exercise = WithLegacy<{
  name: string;
  available: boolean;
  playWordsCount: number;
  wordsColumns: number;
  description: string;
  level: number;
  pictureUrl: string;
  order: number;
  exerciseType: string;
  startTime: Date;
  endTime: Date;
  noise: { level?: number; url?: string };
  isAudioFileUrlGenerated: boolean;
  exerciseMechanism: string;
  template: string;
  active: boolean;
  changedBy: string;
  changedWhen: string;
  series: SeriesLike;
  signals: unknown[];
  tasks: TaskLike[];
  parent: SubgroupRef;
  isManuallyCompleted: boolean;
  sortChildrenBy: string;
  children: TaskLike[];
  sortedChildren: unknown[] | null;
  sortedTasks: unknown[] | null;
  noiseLevel: number;
  noiseUrl: string | null;
  isCompleted: boolean;
  previousSiblings: unknown[];
  siblingExercises: unknown[];
  nextSiblings: unknown[];
  allSiblings: unknown[];
  isFirst: boolean;
  canInteract: boolean;
  isQueryParams: undefined;
  isStarted: boolean;
  stats: { startTime: Date; endTime: Date; exerciseId: string | null | undefined };
  trackTime: (type?: string) => void;
  calcStats: (data: IStatsExerciseStats | undefined) => IStatsObject;
  postHistory: (data: IStatsExerciseStats) => Promise<void>;
  [Type]: 'exercise';
}>;
