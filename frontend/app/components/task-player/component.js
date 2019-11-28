import Component from '@ember/component';
import { A } from '@ember/array';
import { inject } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';

export default class TaskPlayerComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;

  didReceiveAttrs() {
    this.shuffle();
    this.set('lastAnswer', null);
  }

  shuffle() {
    this.set('shuffledWords', A(shuffleArray(this.task.words)));
    this.notifyPropertyChange('shuffledWords');
  }

  onRightAnswer() {}

  handleSubmit(word) {
    this.set('lastAnswer', word);
    if (word !== this.task.word) {
      const currentWordsOrder = Array.from(this.shuffledWords);
      this.task.set('nextAttempt', true);
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
    } else {
      this.task.savePassed();
      this.task.set('nextAttempt', false);
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
