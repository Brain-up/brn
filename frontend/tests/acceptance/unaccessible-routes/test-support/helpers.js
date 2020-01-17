import {
  getTaskScenarioData,
  getExerciseScenarioData,
  getSeriesScenarioData,
} from './data-storage';

export function getUnaccessibleTaskScenario() {
  const { tasks, exercises, series, groups } = getTaskScenarioData();
  return getServerResponses({ tasks, series, groups, exercises });
}

export function getUnaccessibleExerciseScenario() {
  const { tasks, series, groups, exercises } = getExerciseScenarioData();
  return getServerResponses({ tasks, series, groups, exercises });
}

export function getUnaccessibleSeriesScenario() {
  const { tasks, series, groups, exercises } = getSeriesScenarioData();
  return getServerResponses({ tasks, series, groups, exercises });
}

function getServerResponses({ tasks, series, groups, exercises }) {
  /* eslint-disable no-undef */
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

  server.get('groups/:id', (schema, request) => {
    return { data: groups.findBy('id', Number(request.params.id)) };
  });

  server.get('series/:id', (schema, request) => {
    return { data: series.findBy('id', Number(request.params.id)) };
  });

  server.get('exercises/:id', (schema, request) => {
    return { data: exercises.findBy('id', Number(request.params.id)) };
  });
}
