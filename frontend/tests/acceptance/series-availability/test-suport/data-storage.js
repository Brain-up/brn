export function getTestData() {
  const tasks = [];
  const exercises = [];
  const series = [
    {
      id: '1',
      name: 'link-1',
      group: 1,
      type: 'SINGLE_SIMPLE_WORDS',
      level: 1,
      description: '',
      active: true,
      subGroups: [],
    },
    {
      id: '2',
      name: 'link-2',
      group: 1,
      type: 'SINGLE_SIMPLE_WORDS',
      level: 2,
      description: '',
      active: true,
      subGroups: [],
    },
  ];
  const groups = [{ order: 1, id: '1', series: ['1', '2'] }];

  const subgroups = [];

  return { tasks, subgroups, exercises, series, groups };
}
