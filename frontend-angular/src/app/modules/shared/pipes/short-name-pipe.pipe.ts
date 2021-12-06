import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'shortName',
})
export class ShortNamePipe implements PipeTransform {
  transform(fullName: string): string {
    return fullName
      .split(' ')
      .map((n) => n[0])
      .join('');
  }
}
