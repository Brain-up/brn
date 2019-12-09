import Component from '@ember/component';
import { A } from '@ember/array';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import ENV from 'brn/config/environment';

export default class TaskPlayerComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;
  rootURL = ENV.rootURL;

  @service('audio') audio;

  classNames = ['flex-1', 'flex', 'flex-col'];

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
      this.audio.player.playAudio();
    } else {
      this.task.savePassed();
      this.task.set('nextAttempt', false);
    }
  }
}
({});

function shuffleArray(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
