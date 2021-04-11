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

interface Task {
  id: number;
}
