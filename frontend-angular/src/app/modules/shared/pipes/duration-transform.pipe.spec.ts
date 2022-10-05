import { DurationTransformPipe } from './duration-transform.pipe';

describe('DurationTransformPipe', () => {
  const durationTransformPipe = new DurationTransformPipe();

  it(' should create a pipe', () => {
    expect(durationTransformPipe).toBeTruthy();
  });

  it('should return correct value', () => {
    const spentTimeFirst = 3600e9;
    expect(durationTransformPipe.transform(spentTimeFirst)).toEqual('01h:00m:00s');

    const spentTimeSecond = 73220e9;
    expect(durationTransformPipe.transform(spentTimeSecond)).toEqual('20h:20m:20s');

    const spentTimeThird = 21e9;
    expect(durationTransformPipe.transform(spentTimeThird)).toEqual('00h:00m:21s');
  });
});
