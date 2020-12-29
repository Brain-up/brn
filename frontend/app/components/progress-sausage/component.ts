import Component from '@glimmer/component';

interface IProgressSausageComponentArgs {
  progressItems: { completedInCurrentCycle: boolean }[]
}

export default class ProgressSausageComponent extends Component<IProgressSausageComponentArgs> {
  get progressItems() {
    return this.args.progressItems ?? [];
  }

  get progressWidth() {
    return `width: ${this.progress * 100}%`;
  }

  get progress() {
    return (
      this.progressItems.filter(
        ({ completedInCurrentCycle }) => completedInCurrentCycle,
      ).length / this.progressItems.length
    );
  }
}
