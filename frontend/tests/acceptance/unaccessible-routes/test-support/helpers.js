import {
  getTaskScenarioData,
  getExerciseScenarioData,
  getSeriesScenarioData,
} from './data-storage';
import { getServerResponses } from '../../general-helpers';

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
