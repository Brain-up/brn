import { click } from '@ember/test-helpers';

export function getServerResponses({
  tasks,
  series,
  groups,
  subgroups,
  exercises,
  availableExercises,
}) {
  /* eslint-disable no-undef */
  server.get('groups', () => {
    return { data: groups };
  });

  server.get('subgroups', () => {
    return { data: subgroups };
  });

  server.get('series', (schema, request) => {
    const targetGroup = Number(request.queryParams.groupId);
    const seriesIds = groups.findBy('id', targetGroup).series;
    return { data: series.filter((s) => seriesIds.includes(s.id)) };
  });

  server.get('exercises', (schema, request) => {
    const targetSeries = Number(request.queryParams.subGroupId);
    const exercisesIds = subgroups.findBy('id', targetSeries).exercises;
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

  server.post('exercises/byIds', () => {
    return { data: availableExercises ? availableExercises : [] };
  });

  server.post('study-history', () => {
    return { id: 1 };
  });

  server.get('users/current', () => {
    return {"data":[
        {
          "roles":["ADMIN"],
          "id":1,
          "userId":"f89e5760-0caf-4a95-9810-cd6aa4a8261e",
          "name":"admin",
          "email":"admin@admin.com",
          "bornYear":1999,
          "gender":"MALE",
          "active":true,
          "created":"2021-07-30T08:13:15.587",
          "changed":"2021-12-01T12:04:10.65",
          "avatar":"18",
          "headphones":[
              {"id":1,"name":"first","active":true,"type":"ON_EAR_BLUETOOTH","description":"first","userAccount":1}
          ]
        }
      ],
      "errors":[],
      "meta":[]
    };
  });
}

export async function continueAfterStats() {
  await click(`[data-test-continue]`);
}

export async function chooseAnswer(option) {
  await click(`[data-test-task-answer-option="${option}"]`);
}
