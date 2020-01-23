import { getData } from './data-storage';
import pageObject from './page-object';

export function setupAfterPageVisit() {
  const data = getData();
  const targetTask = data.tasks.find(
    (task) => task.id === Number(pageObject.currentTaskId),
  );

  const wrongAnswer = targetTask.answerOptions.find(
    (option) => option.id !== targetTask.correctAnswer.id,
  );
  return { targetTask, wrongAnswer };
}
