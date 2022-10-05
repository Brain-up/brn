import { NgModule } from '@angular/core';
import { ShortNamePipe } from './short-name.pipe';
import { DurationTransformPipe } from './duration-transform.pipe';

@NgModule({
  declarations: [ShortNamePipe, DurationTransformPipe],
  exports: [ShortNamePipe, DurationTransformPipe],
})
export class PipesModule {}
