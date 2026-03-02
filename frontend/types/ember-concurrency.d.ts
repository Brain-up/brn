/**
 * Module augmentation for ember-concurrency's classic decorator pattern.
 *
 * ember-concurrency 5.x ships types focused on the async arrow function pattern,
 * but at runtime it still supports the classic `@(task(function*(){}).drop())` pattern.
 * This augmentation adds proper type support for generator-based task decorators.
 *
 * IMPORTANT: Because tsconfig `paths` maps `"*"` to `"types/*"`, this file is
 * resolved as the primary module for `ember-concurrency`. We must therefore
 * re-export every public symbol that consumers import from the package, in
 * addition to our generator-based overload.
 */

// Pull everything from the real package (resolved via node_modules)
// and re-export so that `import { Task, timeout, ... } from 'ember-concurrency'`
// keeps working.
export {
  Task,
  TaskInstance,
  TaskProperty,
  TaskCancelation,
  Yieldable,
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
declare module 'ember-concurrency' {
  export function task<T = any, Args extends any[] = any[]>(
    fn: (...args: any[]) => Generator<any, T, any>,
  ): TaskProperty<T, Args>;
}
