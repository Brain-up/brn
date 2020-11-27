export function createStubTasks(store, tasksInfo) {
  let parentOne = store.createRecord('series', {});
  let parent = store.createRecord('exercise', { parent: parentOne });
  return tasksInfo.map((taskInfo) =>
    store.createRecord('task', { ...taskInfo, id: taskInfo.order, parent }),
  );
}
