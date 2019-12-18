import EmberObject from '@ember/object';

export default class StudyHistory extends EmberObject.extend({}) {}

StudyHistory.reopenClass({
  generate({
    userId,
    startTime,
    endTime,
    exerciseId,
    tasksCount,
    repetitionIndex,
  }) {
    return StudyHistory.create({
      userId,
      startTime,
      endTime,
      exerciseId,
      tasksCount,
      repetitionIndex,
    });
  },
});
