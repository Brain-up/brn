import Service, { service } from '@ember/service';
import { A } from '@ember/array';
import { tracked } from '@glimmer/tracking';
import type NetworkService from 'brn/services/network';

export default class TasksManagerService extends Service {
  @service('network') network!: NetworkService;

  @tracked
  completedTasks = A();
  @tracked
  completedCycleTasks = A();
  @tracked
  completedExerciseIds: Set<string> = new Set();
  saveAsCompleted(task: any) {
    this.completedTasks = A([...this.completedTasks, task]);
    this.completedCycleTasks = A([...this.completedCycleTasks, task]);
  }
  isCompleted(task: any) {
    return this.completedTasks.includes(task);
  }
  isCompletedInCurrentCycle(task: any) {
    return this.completedCycleTasks.includes(task);
  }
  async loadTodayCompletedExercises() {
    try {
      const now = new Date();
      const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
      const pad = (n: number) => String(n).padStart(2, '0');
      const fmt = (d: Date) =>
        `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
      const data: Array<{ exerciseId?: number | string }> = await this.network.getStudyHistoriesV2(fmt(start), fmt(end));
      const ids = new Set<string>();
      for (const entry of data) {
        if (entry.exerciseId != null) {
          ids.add(String(entry.exerciseId));
        }
      }
      this.completedExerciseIds = ids;
    } catch {
      // silent failure — don't block the app
    }
  }
  clearCompletedTasks() {
    this.completedTasks = A();
  }
  clearCurrentCycleTaks() {
    this.completedCycleTasks = A();
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    'tasks-manager': TasksManagerService;
  }
}
