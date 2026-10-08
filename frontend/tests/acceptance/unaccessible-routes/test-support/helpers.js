import {
  getTaskScenarioData,
  getExerciseScenarioData,
} from './data-storage';
import { getServerResponses } from '../../general-helpers';

export function getUnaccessibleTaskScenario() {
  const { tasks, exercises, series, groups, subgroups } = getTaskScenarioData();
  return getServerResponses({ tasks, series, groups, exercises, subgroups });
}

export function getUnaccessibleExerciseScenario() {
  const { tasks, series, groups, exercises, subgroups } = getExerciseScenarioData();
  return getServerResponses({ tasks, series, groups, exercises, subgroups });
}
