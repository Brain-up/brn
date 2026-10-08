import { click } from '@ember/test-helpers';

export function getServerResponses({
  tasks,
  series,
  groups,
  subgroups,
  exercises,
  availableExercises,
}) {
  server.get('groups', () => {
    return { data: groups };
  });

  server.get('subgroups', () => {
    return { data: subgroups };
  });

  server.get('series', (request) => {
    const targetGroup = request.queryParams.groupId;
    const group = groups.find((g) => g.id === targetGroup);
    const seriesIds = group ? group.series : [];
    return { data: series.filter((s) => seriesIds.includes(s.id)) };
  });

  server.get('exercises', (request) => {
    const targetSeries = request.queryParams.subGroupId;
    const subgroup = subgroups.find((sg) => sg.id === targetSeries);
    const exercisesIds = subgroup ? subgroup.exercises : [];
    return { data: exercises.filter((e) => exercisesIds.includes(e.id)) };
  });

  server.get('tasks', () => {
    return { data: tasks };
  });

  server.get('tasks/:id', (request) => {
    return { data: tasks.find((t) => t.id === request.params.id) };
  });

  server.get('groups/:id', (request) => {
    return { data: groups.find((g) => g.id === request.params.id) };
  });

  server.get('series/:id', (request) => {
    return { data: series.find((s) => s.id === request.params.id) };
  });

  server.get('exercises/:id', (request) => {
    return { data: exercises.find((e) => e.id === request.params.id) };
  });

  server.post('exercises/byIds', () => {
    return { data: availableExercises ? availableExercises : [] };
  });

  server.post('study-history', () => {
    return { id: '1' };
  });

  server.get('users/current', () => {
    return {"data":[
        {
          "roles":["ADMIN"],
          "id":"1",
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
              {"id":"1","name":"first","active":true,"type":"ON_EAR_BLUETOOTH","description":"first","userAccount":"1"}
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
  const selector = `[data-test-task-answer-option="${option}"]`;
  const el = document.querySelector(selector);
  if (!el) return;
  if (el.disabled) {
    // @ember/test-helpers 4.x throws on clicking disabled elements;
    // wait for it to become enabled (e.g. transition between questions).
    await new Promise((r) => setTimeout(r, 100));
    const enabled = document.querySelector(`${selector}:not(:disabled)`);
    if (!enabled) return;
    await click(enabled);
  } else {
    await click(el);
  }
}
