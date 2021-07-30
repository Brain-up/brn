import Route from '@ember/routing/route';
import { DateTime } from 'luxon';

export default class StatisticsRoute extends Route {
  get initialSelectedMonth(): DateTime {
    return DateTime.now();
  }
}
