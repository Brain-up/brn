export interface SeriesModel {
  group: number;
  id: number;
  name: string;
  description: string;
  excercises: Array<number>; // TODO: check if there is a typo here: 'excercises' => 'exercises'
}
