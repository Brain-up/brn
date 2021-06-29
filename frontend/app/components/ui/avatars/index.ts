import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';

interface IComponentArguments {
  onSelect?: (id?: number) => void;
  onSubmit?: () => void;
  onCancel?: () => void;
}

export default class UiAvatarsComponent extends Component<IComponentArguments> {
  @service('user-data') userData!: UserDataService;
  @tracked selectedAvatar = 0;

  get activeTab() {
    return this.selectedAvatar || parseInt(this.userData.selectedAvatarId, 10);
  }

  @action onSelect(id: number) {
    this.selectedAvatar = id;
    this.args.onSelect?.(id);
  }

  get avatars() {
    return new Array(20).fill(0).map((_, index) => {
      return `/pictures/avatars/avatar ${index + 1}.png`;
    });
  }
}
