import { secondsTo } from './seconds-to';

describe('secondsTo', () => {
  it('should return minutes, seconds', () => {
    expect(secondsTo(10, 'm:s')).toEqual('00:10');
  });

  it('should return hours, minutes, seconds', () => {
    expect(secondsTo(3600, 'h:m:s')).toEqual('01:00:00');
  });
});
