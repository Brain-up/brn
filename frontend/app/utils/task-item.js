import { tracked } from '@glimmer/tracking';

export class TaskItem {
	@tracked isCompleted;
	@tracked canInteract;
	@tracked order;
	@tracked completedInCurrentCycle;
	@tracked nextAttempt;
	@tracked answer = [];
	constructor(params = {}) {
	  Object.assign(this, params);
	}
  }
  