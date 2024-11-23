import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'durationTransform',
    standalone: false
})
export class DurationTransformPipe implements PipeTransform {
  transform(spentTime: number): string {
    if (spentTime) {
      const fullSeconds = spentTime / 1e9;
      let hours = Math.floor(fullSeconds / 3600).toString();
      let minutes = Math.floor(fullSeconds % 3600 / 60).toString();
      let seconds = (fullSeconds % 60).toString();
      hours = + hours < 10 ? '0' + hours : hours;
      minutes = + minutes < 10 ? '0' + minutes : minutes;
      seconds = + seconds < 10 ? '0' + seconds : seconds;

      return `${hours}h:${minutes}m:${seconds}s`;
    }
    return '0';
  }
}
