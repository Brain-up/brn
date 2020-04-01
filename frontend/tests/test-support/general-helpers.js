export function createStubTasks(store, tasksInfo) {
  let parent = store.createRecord('series', {});
  return tasksInfo.map((taskInfo) =>
    store.createRecord('task', { ...taskInfo, id: taskInfo.order, parent }),
  );
}
