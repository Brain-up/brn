import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
export default class StudyConfigService extends Service {
    @tracked
    showImages = true;

    @action
    toggleImageVisibility() {
        this.showImages = !this.showImages;
    }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
    interface Registry {
      'study-config': StudyConfigService;
    }
  }
  