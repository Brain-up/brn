import Component from '@glimmer/component';

export default class ExerciseStatsComponent extends Component {
    get minutes() {
        const { endTime, startTime } = this.args.stats;
        return ((endTime.getTime() - startTime.getTime()) / 1000 / 60).toFixed(2);
    }
    get repetitionIndex() {
        return this.args.stats.repetitionIndex.toFixed(2);
    }
}
