import { set } from '@ember/object';
import deepCopy from 'brn/utils/deep-copy';
import { tracked } from '@glimmer/tracking';

export function completeByOrder(items, order) {
  const target = items.findBy('order', order);
  set(target, 'completedInCurrentCycle', true);
}

class DefaultItem {
  @tracked isCompleted;
  @tracked canInteract;
  @tracked order;
  @tracked completedInCurrentCycle;
  constructor(params) {
    Object.assign(this, params);
  }
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
    resultArray.push(
      new DefaultItem({
        ...deepCopy(defaultItem),
        order: counter,
      }),
    );
    counter++;
  }

  return resultArray;
}
