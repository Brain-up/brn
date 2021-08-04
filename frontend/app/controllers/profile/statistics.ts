import Controller from '@ember/controller';
import { DateTime } from 'luxon';

export default class ProfileStatisticsController extends Controller {
  get initialSelectedMonth(): DateTime {
    return DateTime.now();
  }
}
