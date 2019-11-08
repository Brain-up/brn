export function createStubTasks(store, tasksInfo) {
  return tasksInfo.map((taskInfo) =>
    store.createRecord('task', { ...taskInfo, id: taskInfo.order }),
  );
}
