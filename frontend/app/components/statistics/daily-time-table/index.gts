import './index.css';
import Component from '@glimmer/component';
import {DateTime} from "luxon";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {tracked} from "@glimmer/tracking";
import type { UserDailyTimeTableStatistics as UserDailyTimeTableStatisticsModel } from "brn/schemas/user-daily-time-table-statistics";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {service} from "@ember/service";
import type Store from 'brn/services/store';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {action} from "@ember/object";
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { t } from 'ember-intl';
import LoadingSpinner from 'brn/components/loading-spinner';

interface DailyTimeTableSignature {
  Args: {
  day: DateTime;
  };
  Element: HTMLElement;
}

export default class DailyTimeTableComponent extends Component<DailyTimeTableSignature> {

  @tracked isLoading = true;
  @tracked userDailyDetailsData: UserDailyTimeTableStatisticsModel[] | null = null;
  @service('store') store!: Store;

  @action
  didInit() {
    this.loadData();
  }

  async loadData() {
    this.isLoading = true;
    try {
      const data = await this.store.query<UserDailyTimeTableStatisticsModel>(
        'user-daily-time-table-statistics',
        {
          day: this.args.day,
        },
      );
      this.userDailyDetailsData = data.slice();
    } catch (error) {
      console.error(error);
    }
    this.isLoading = false;
  }

  <template>
    <div
      class="c-daily-time-table"
      ...attributes
      {{didInsert this.didInit}}
      {{didUpdate this.didInit @day}}
    >
      <div class="sm:px-5 box-content flex items-center justify-center px-2 py-6">
        {{#if this.isLoading}}
          <LoadingSpinner />
        {{else}}
          <div class="w-full overflow-x-auto">
            <table class="w-full table-auto">
              <thead>
              <tr>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.series_name"}}</th>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.all_done_exercises"}}</th>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.unique_done_exercises"}}</th>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.repeated_exercises"}}</th>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.done_exercises_successfully_from_first_time"}}</th>
                <th class="px-4 py-2">{{t "profile.statistics.daily_time_table.listen_words_count"}}</th>
              </tr>
              </thead>
              <tbody>
                {{#each this.userDailyDetailsData as |element|}}
                  <tr>
                    <td class="px-4 py-2 border">{{element.seriesName}}</td>
                    <td class="px-4 py-2 border">{{element.allDoneExercises}}</td>
                    <td class="px-4 py-2 border">{{element.uniqueDoneExercises}}</td>
                    <td class="px-4 py-2 border">{{element.repeatedExercises}}</td>
                    <td class="px-4 py-2 border">{{element.doneExercisesSuccessfullyFromFirstTime}}</td>
                    <td class="px-4 py-2 border">{{element.listenWordsCount}}</td>
                  </tr>
                {{/each}}
              </tbody>
            </table>
          </div>
        {{/if}}
    
      </div>
    </div>
  </template>
}
