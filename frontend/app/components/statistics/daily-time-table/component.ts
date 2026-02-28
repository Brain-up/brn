import Component from '@glimmer/component';
import {DateTime} from "luxon";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {tracked} from "@glimmer/tracking";
import type UserDailyTimeTableStatisticsModel from "brn/models/user-daily-time-table-statistics";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {inject as service} from "@ember/service";
import type Store from 'brn/services/store';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {action} from "@ember/object";

interface IDailyTimeTableComponentArgs {
  day: DateTime;
}

export default class DailyTimeTableComponent extends Component<IDailyTimeTableComponentArgs> {

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
}
