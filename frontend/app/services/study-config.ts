import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import Task from 'brn/models/task';

export default class StudyConfigService extends Service {
    @tracked
    _showImages = true;

    get task(): Task | undefined {
      return getOwner(this).lookup('controller:group.series.subgroup.exercise.task').model;
    }

    get allowImagesInTask() {
      return this.task?.shouldBeWithPictures ?? false;
    }

    get showImageToggler() {
      return this.allowImagesInTask;
    }

    get showImages() {
      return this._showImages && this.allowImagesInTask;
    }

    @action
    toggleImageVisibility() {
      this._showImages = !this._showImages;
    }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
    interface Registry {
      'study-config': StudyConfigService;
    }
  }
  