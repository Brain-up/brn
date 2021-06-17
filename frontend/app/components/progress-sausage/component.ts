import Component from '@glimmer/component';

interface IProgressSausageComponentArgs {
  progressItems: { completedInCurrentCycle: boolean }[];
}

export default class ProgressSausageComponent extends Component<IProgressSausageComponentArgs> {
  get progressItems() {
    return this.args.progressItems ?? [];
  }

  get progressWidth() {
    return `width:${Math.round(this.progress * 100)}%;`;
  }

  get progress() {
    const items = this.progressItems;
    if (!items.length) {
      return 0;
    }
    return (
      items.filter(({ completedInCurrentCycle }) => completedInCurrentCycle)
        .length / items.length
    );
  }
}
