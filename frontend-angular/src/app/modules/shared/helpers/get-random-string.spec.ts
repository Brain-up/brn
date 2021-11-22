import { getRandomString } from './get-random-string';

describe('getRandomString', () => {
  it('should return random string', () => {
    const chars = getRandomString();
    expect(getRandomString().length).toEqual(chars.length);
  });

  it('should return empty string on zero', () => {
    const length = 0;
    expect(getRandomString(length)).toEqual('');
  });
});
