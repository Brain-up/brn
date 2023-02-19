import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';

interface IComponentArguments {
  selectedAvatar: string;
  onSubmit: (avatar: string) => void;
  onCancel: () => void;
}

export default class UiAvatarsComponent extends Component<IComponentArguments> {
  @tracked preferredAvatar!: string;

  @action storeCurrentAvatar() {
    this.preferredAvatar = this.args.selectedAvatar;
  }

  @action onSelect(avatar: string) {
    this.preferredAvatar = avatar;
  }

  @action onSubmit() {
    this.args.onSubmit(this.preferredAvatar);
  }

  get avatars() {
    return new Array(20).fill(0).map((_, index) => (index + 1).toString());
  }
}
