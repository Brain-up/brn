import Component from '@ember/component';
import { A } from '@ember/array';
import { inject } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';

export default class TaskPlayerComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;

  onFinished() {}

  didReceiveAttrs() {
    this.shuffle();
    this.set('lastAnswer', null);
  }

  shuffle() {
    this.set('shuffledWords', A(shuffleArray(this.task.words)));
    this.notifyPropertyChange('shuffledWords');
  }

  handleSubmit(word) {
    this.set('lastAnswer', word);
    if (word !== this.task.word) {
      const currentWordsOrder = Array.from(this.shuffledWords);
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
    }
  }
}
({
  router: inject(),
});

function shuffleArray(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
