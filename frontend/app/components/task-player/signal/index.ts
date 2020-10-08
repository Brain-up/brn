import Component from '@glimmer/component';
import TaskSignalModel from 'brn/models/task/signal';
import { MODES } from 'brn/utils/task-modes';
import { action } from '@ember/object';
import Tone from 'tone';

interface ISignalComponentArgs {
  task: TaskSignalModel,
  mode: keyof (typeof MODES),
  disableAnswers: boolean,
  activeWord: string,
  disableAudioPlayer: boolean,
  onPlayText(): void,
  onRightAnswer(): void,
  onWrongAnswer(): void
}


export default class TaskPlayerSignalComponent extends Component<ISignalComponentArgs> {
  get tasksCopy() {
    return this.args.task.get('parent').get('tasks').toArray();
  }

  @action checkMaybe() {

  }

  @action onInsert() {
    const { duration, frequency } = this.args.task.signal;
    const synth = new Tone.PolySynth(Tone.Synth).toDestination();
    synth.triggerAttack(frequency, Tone.now())
    setTimeout(()=>{
      synth.dispose();
    }, duration);
  }
}
