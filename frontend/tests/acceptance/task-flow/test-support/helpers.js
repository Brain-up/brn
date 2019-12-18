import { getData } from './data-storage';
import { click } from '@ember/test-helpers';
import pageObject from './page-object';

const { tasks, series, groups, exercises } = getData();

export function setupAfterPageVisit() {
  const data = getData();
  const targetTask = data.tasks.find(
    (task) => task.id === Number(pageObject.currentTaskId),
  );

  const wrongAnswer = targetTask.answerOptions.find(
    (option) => option.id !== targetTask.correctAnswer.id,
  );
  return { targetTask, wrongAnswer };
}

export function getServerResponses() {
  /* eslint-disable no-undef */
  server.post('study-history', () => {
    return { id: 1 };
  });

  server.get('groups', () => {
    return { data: groups };
  });

  server.get('series', (schema, request) => {
    const targetGroup = Number(request.queryParams.groupId);
    const seriesIds = groups.findBy('id', targetGroup).series;
    return { data: series.filter((s) => seriesIds.includes(s.id)) };
  });

  server.get('exercises', (schema, request) => {
    const targetSeries = Number(request.queryParams.seriesId);
    const exercisesIds = series.findBy('id', targetSeries).exercises;
    return { data: exercises.filter((e) => exercisesIds.includes(e.id)) };
  });

  server.get('tasks', () => {
    return { data: tasks };
  });

  server.get('tasks/:id', (schema, request) => {
    return { data: tasks.findBy('id', Number(request.params.id)) };
  });

  server.get('series/:id', (schema, request) => {
    return { data: series.findBy('id', Number(request.params.id)) };
  });

  server.get('exercises/:id', (schema, request) => {
    return { data: exercises.findBy('id', Number(request.params.id)) };
  });
}

export async function chooseAnswer(option) {
  await click(`[data-test-task-answer-option=${option}]`);
}
