import { attr } from '@ember-data/model';
import Model from '@ember-data/model';

export default class UserDailyTimeTableStatisticsModel extends Model {
  @attr('string') seriesName!: string;
  @attr('number') allDoneExercises!: number;
  @attr('number') uniqueDoneExercises!: number;
  @attr('number') repeatedExercises!: number;
  @attr('number') doneExercisesSuccessfullyFromFirstTime!: number;
  @attr('number') listenWordsCount!: number;

  toString() {
    return "seriesName: " + this.seriesName + "," +
    "allDoneExercises: " + this.allDoneExercises + "," +
    "uniqueDoneExercises: " + this.uniqueDoneExercises + "," +
    "repeatedExercises: " + this.repeatedExercises + "," +
    "doneExercisesSuccessfullyFromFirstTime: " + this.doneExercisesSuccessfullyFromFirstTime + "," +
    "listenWordsCount: " + this.listenWordsCount;
  }

}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    userDailyTimeTableStatisticsModel: UserDailyTimeTableStatisticsModel;
  }
}
