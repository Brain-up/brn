import Service from '@ember/service';
import StudyHistory from 'brn/models/study-history';

export default Service.extend({
  async saveHistory(props) {
    const historyUnit = StudyHistory.generate({ ...props });

    await fetch('/api/study-history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(historyUnit),
    });
  },
  saveExerciseHistory(exercise) {
    const { id, startTime, endTime, tasks } = exercise;

    const repetitionsCount = exercise.tasks.reduce((result, task) => {
      if (task.repetitionCount) {
        result += task.repetitionCount;
      }
      return result;
    }, 0);

    const repetitionIndex = repetitionsCount / tasks.length;

    return this.saveHistory({
      startTime,
      endTime,
      repetitionIndex,
      exerciseId: id,
      tasksCount: tasks.length,
    });
  },
});
