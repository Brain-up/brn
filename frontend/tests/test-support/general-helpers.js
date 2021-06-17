export function createStubTasks(store, tasksInfo) {
  let parentOne = store.createRecord('series', {});
  let subGroup = store.createRecord('subgroup', { parent: parentOne });
  let parent = store.createRecord('exercise', { parent: subGroup });
  return tasksInfo.map((taskInfo) =>
    store.createRecord('task', { ...taskInfo, id: taskInfo.order, parent }),
  );
}
