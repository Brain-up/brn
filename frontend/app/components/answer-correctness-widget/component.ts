import Component from '@glimmer/component';

interface IAnswerCorrectnessWidgetComponentArgs {
  isCorrect: boolean;
}

function getRandomInt(min: number, max: number) {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default class AnswerCorrectnessWidgetComponent extends Component<IAnswerCorrectnessWidgetComponentArgs> {
  maxImagesNumber = 8;
  get imagePath() {
    const randomNumber = this.getAllowedRandomNumber();
    return `${
      this.args.isCorrect ? 'victory/victory' : 'regret/regret'
    }${randomNumber}`;
  }
  getAllowedRandomNumber(): number {
    return getRandomInt(1, this.maxImagesNumber);
  }
  get itemStyle() {
    const img = this.imagePath;
    return `
      background-image:
        url('/pictures/${img}.jpg'),
        url('/pictures/${img}.png'),
        url('/pictures/${img}.jpeg'),
        url('/pictures/${img}.svg');
    `.trim();
  }
}
