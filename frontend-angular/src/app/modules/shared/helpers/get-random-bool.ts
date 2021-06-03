import { getRandomIntInclusive } from './get-random-int-inclusive';

export function getRandomBool(): boolean {
  return Boolean(getRandomIntInclusive(0, 1));
}
