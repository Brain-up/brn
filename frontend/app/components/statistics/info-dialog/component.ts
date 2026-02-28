import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';

export default class StatisticsInfoDialogComponent extends Component {
  @service declare intl: IntlService;

  get infoDialogImage(): string {
    if (this.intl.primaryLocale === 'ru-ru') {
      return '/ui/statistics-info-dialog.svg';
    }
    return '/ui/statistics-info-dialog-en.svg';
  }
}
