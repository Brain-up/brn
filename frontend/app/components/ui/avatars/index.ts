import Component from '@glimmer/component';

interface IComponentArguments {
  onSelect?: (id: number) => void
}

export default class UiAvatarsComponent extends Component<IComponentArguments> {
  get avatars() {
    return new Array(20).fill(0).map((_, index) => {
      return `/pictures/avatars/avatar ${index + 1}.png`;
    })
  }
}
