import { helper } from '@ember/component/helper';

export function queue(actions: ((...args: unknown[]) => unknown)[]): (...args: unknown[]) => Promise<void> {
  return async function (...args: unknown[]) {
    for (const action of actions) {
      if (typeof action === 'function') {
        const result = action(...args);
        if (result && typeof (result as Promise<unknown>).then === 'function') {
          await result;
        }
      }
    }
  };
}

export default helper(queue);
