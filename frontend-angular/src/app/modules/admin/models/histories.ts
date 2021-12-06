export interface Histories {
  data: HistoriesData[];
  errors: any[];
  meta: any[];
}

export interface HistoriesData {
  endTime: Date;
  executionSeconds: number;
  exerciseId: number;
  id: number;
  replaysCount: number;
  rightAnswersCount: number;
  startTime: Date;
  tasksCount: number;
}
