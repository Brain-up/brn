import { ShortNamePipe } from './short-name.pipe';

describe('ShortNamePipe', () => {
  const shortNamePipe = new ShortNamePipe();

  it('create an instance', () => {
    expect(shortNamePipe).toBeTruthy();
  });

  it('should return correct value', () => {
    expect(shortNamePipe.transform('Name Name')).toEqual('NN');
  });

  it('should return correct value if no name', () => {
    expect(shortNamePipe.transform('')).toEqual('-');
  });
});
