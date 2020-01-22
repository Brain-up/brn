import { set } from '@ember/object';
import deepCopy from 'brn/utils/deep-copy';

export function completeByOrder(items, order) {
  const target = items.findBy('order', order);
  set(target, 'completedInCurrentCycle', true);
}

export function getLongItemsList() {
  let counter = 0;
  const listLength = 100;
  const resultArray = [];

  const defaultItem = {
    isCompleted: false,
    canInteract: true,
  };
  while (counter < listLength) {
    resultArray.push({
      ...deepCopy(defaultItem),
      order: counter,
    });
    counter++;
  }

  return resultArray;
}
