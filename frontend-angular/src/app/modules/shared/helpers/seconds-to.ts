export function secondsTo(totalSeconds: number, format: 'm:s' | 'h:m:s'): string {
  const hours = Math.trunc(totalSeconds / 3600);
  const minutes = Math.trunc((totalSeconds - hours * 3600) / 60);
  const seconds = totalSeconds - hours * 3600 - minutes * 60;

  switch (format) {
    case 'm:s':
      return `${makeMinTwoDigits(minutes)}:${makeMinTwoDigits(seconds)}`;

    case 'h:m:s':
      return `${makeMinTwoDigits(hours)}:${makeMinTwoDigits(minutes)}:${makeMinTwoDigits(seconds)}`;
  }
}

function makeMinTwoDigits(number: number): string {
  const numberAsString = String(number);

  return numberAsString.length === 1 ? `0${number}` : numberAsString;
}
