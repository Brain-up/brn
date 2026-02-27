import Model, { attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';

export default class UserDailyTimeTableStatisticsModel extends Model {
  declare [Type]: 'user-daily-time-table-statistics';
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

