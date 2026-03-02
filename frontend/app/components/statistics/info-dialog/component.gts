import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';
import { t } from 'ember-intl';
import { on } from '@ember/modifier';
import ModalDialog from 'ember-modal-dialog/components/modal-dialog';

export default class StatisticsInfoDialogComponent extends Component {
  @service declare intl: IntlService;

  get infoDialogImage(): string {
    if (this.intl.primaryLocale === 'ru-ru') {
      return '/ui/statistics-info-dialog.svg';
    }
    return '/ui/statistics-info-dialog-en.svg';
  }

  <template>
    <ModalDialog
      @overlayClass="p-8 z-50 min-h-full w-full fixed flex"
      @containerClass="max-w-4xl flex bg-white rounded-lg text-2xl m-auto p-5"
    >
      <div data-test-info-dialog>
        <div class="flex justify-between block pb-5">
          <h1
            class="text-purple-primary text-lg not-italic font-semibold leading-6 uppercase"
          >
            {{t "profile.statistics.info_dialog.title"}}
          </h1>
          <button
            data-test-button-close
            type="button"
            class="btn-press focus:outline-none"
            {{on "click" @closeModalAction}}
          >
            <img src="/ui/close-cross.svg" alt="Close" />
          </button>
        </div>
        <div class="mb-5">
          <p class="mb-10 text-base text-left">
            {{t "profile.statistics.info_dialog.sub_title"}}
          </p>
          <img
            data-test-info-image
            class="w-full"
            src={{this.infoDialogImage}}
            alt="Statistics info"
          />
          <p class="p-2 text-sm text-justify bg-gray-200 rounded-lg">
            {{t "profile.statistics.info_dialog.info" htmlSafe=true}}
          </p>
        </div>
        <div class="flex justify-center">
          <button
            data-test-button-ok
            type="button"
            class="btn-press border-purple-left bg-gradient-to-r from-purple-left to-purple-right rounded-large px-4 py-2 text-base font-medium text-white uppercase border shadow-lg outline-none"
            {{on "click" @closeModalAction}}
          >
            {{t "profile.statistics.info_dialog.button_ok"}}
          </button>
        </div>
      </div>
    </ModalDialog>
  </template>
}
