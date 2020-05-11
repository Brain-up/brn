import Component from '@glimmer/component';

export default class AnswerCorrectnessWidgetComponent extends Component {
  get maxImagesNumber() {
    return this.args.maxImagesNumber || 8;
  }
  get imagePath() {
    let randomNumber = this.getAllowedRandomNumber();
    return `${
      this.args.isCorrect ? 'victory/victory' : 'regret/regret'
    }${randomNumber}`;
  }
  getAllowedRandomNumber() {
    const result = Math.floor(Math.random() * 10) || 1;
    return result <= this.maxImagesNumber
      ? result
      : this.getAllowedRandomNumber();
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
