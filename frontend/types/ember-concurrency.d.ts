/**
 * Type shim for ember-concurrency that adds classic decorator pattern support.
 *
 * Because tsconfig `paths` maps `"*"` to `"types/*"`, this file is resolved
 * as the primary module for `ember-concurrency`. We re-export all public
 * symbols from the real package and add a generator function overload for
 * `task()` that returns `TaskProperty` (with `.drop()`, `.keepLatest()`, etc.).
 *
 * This approach works with `moduleResolution: "bundler"` where the re-export
 * source resolves to node_modules (not back to this file).
 */

// Re-export all public symbols from the real package
export {
  type Task,
  type TaskInstance,
  type TaskProperty,
  type TaskCancelation,
  type Yieldable,
  type TaskForAsyncTaskFunction,
  type AsyncArrowTaskFunction,
  timeout,
  rawTimeout,
  forever,
  animationFrame,
  didCancel,
  all,
  allSettled,
  hash,
  hashSettled,
  race,
  waitForEvent,
  waitForProperty,
  waitForQueue,
  task,
  dropTask,
  enqueueTask,
  keepLatestTask,
  restartableTask,
  registerModifier,
  getModifier,
  hasModifier,
} from 'ember-concurrency';

// Overload: task() accepting a generator function returns TaskProperty
// (which has .drop(), .keepLatest(), .enqueue(), etc.)
// TODO: Tighten generator overload types once migration is stable.
// Currently defaults to `any` for pragmatic migration compatibility.
declare module 'ember-concurrency' {
  export function task<T = any, Args extends any[] = any[]>(
    fn: (...args: any[]) => Generator<any, T, any>,
  ): TaskProperty<T, Args>;
}
