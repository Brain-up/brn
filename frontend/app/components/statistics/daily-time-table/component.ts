import Component from '@glimmer/component';
import {DateTime} from "luxon";
import {tracked} from "@glimmer/tracking";
import UserDailyTimeTableStatisticsModel from "brn/models/user-daily-time-table-statistics";
import {inject as service} from "@ember/service";
import Store from "@ember-data/store";
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
      const data = await this.store.query(
        'user-daily-time-table-statistics',
        {
          day: this.args.day,
        },
      );
      this.userDailyDetailsData = data.toArray();
    } catch (error) {
      console.error(error);
    }
    this.isLoading = false;
  }
}
