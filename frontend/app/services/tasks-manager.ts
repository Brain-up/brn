import Service from '@ember/service';
import { A } from '@ember/array';
import { tracked } from '@glimmer/tracking';

export default class TasksManagerService extends Service {
  @tracked
  completedTasks = A();
  @tracked
  completedCycleTasks = A();
  saveAsCompleted(task: any) {
    this.completedTasks.pushObject(task);
    this.completedCycleTasks.pushObject(task);
  }
  isCompleted(task: any) {
    return this.completedTasks.includes(task);
  }
  isCompletedInCurrentCycle(task: any) {
    return this.completedCycleTasks.includes(task);
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
