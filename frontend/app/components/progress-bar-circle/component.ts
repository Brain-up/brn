import Component from '@glimmer/component';

interface IProgressBarCircleArgs {
  percent: number;
}

export default class ProgressBarCircleComponent extends Component<IProgressBarCircleArgs> {
  get dasharrayValue() {
    let progress = this.args.percent;
    if (isNaN(progress)) {
      progress = 100;
    } else {
      if (progress < 0) {
        progress = 0;
      }
      if (progress > 100) {
        progress = 100;
      }
    }
    return Math.floor(progress);
  }
}
