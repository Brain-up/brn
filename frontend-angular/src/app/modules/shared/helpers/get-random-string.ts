import { getRandomIntInclusive } from './get-random-int-inclusive';

export function getRandomString(length = 10): string {
  const chars = 'abcdefghijklmnopqrstuvwxyz';
  let str = '';

  for (let i = 0; i < length; i++) {
    str += chars.charAt(getRandomIntInclusive(0, chars.length - 1));
  }

  return str;
}
