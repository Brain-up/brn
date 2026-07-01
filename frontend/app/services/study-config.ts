import Service from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import type { TaskBase as Task } from 'brn/schemas/task';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

export default class StudyConfigService extends Service {
    @tracked
    _showImages = true;

    get task(): Task | undefined {
      return (getOwner(this)!.lookup('controller:group.series.subgroup.exercise.task') as { model?: Task } | undefined)?.model;
    }

    get allowImagesInTask() {
      return this.task?.shouldBeWithPictures ?? false;
    }

    get showImageToggler() {
      return this.allowImagesInTask;
    }

    get allowSpeechRate(): boolean {
      // Speech-rate only affects decoded audio buffers (words, sounds, prosody).
      // Pure-tone SIGNALS exercises play through Tone synths and are unaffected,
      // so the control would be misleading there. Show it everywhere else.
      return this.task?.exerciseMechanism !== ExerciseMechanism.SIGNALS;
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
  