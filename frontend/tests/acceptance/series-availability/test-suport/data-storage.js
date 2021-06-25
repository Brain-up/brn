export function getTestData() {
  const tasks = [];
  const exercises = [];
  const series = [
    {
      order: 1,
      id: 1,
      name: 'link-1',
    },
    {
      order: 2,
      id: 2,
      name: 'link-2',
    },
  ];
  const groups = [{ order: 1, id: 1, series: [1, 2] }];

  const subgroups = [];

  return { tasks, subgroups, exercises, series, groups };
}
