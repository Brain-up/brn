export interface Exercise {
  seriesId: string;
  id: string;
  name: string;
  level: number;
  noise: Noise;
  template: any;
  available: boolean;
  tasks: Array<Task>;
  signals: Array<any>;
}

interface Noise {
  level: number;
  url: string;
}

export interface Task {
  id: number;
  level: number;
  exerciseType: string;
  name: string;
  serialNumber: number;
  answerOptions: Array<Answer>;
}

export interface Answer {
  id: number;
  audioFileUrl: string;
  word: string;
  wordType: string;
  pictureFileUrl: string;
  soundsCount: number;
}
