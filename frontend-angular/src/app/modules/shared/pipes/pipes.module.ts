import { NgModule } from '@angular/core';
import { ShortNamePipe } from './short-name.pipe';

@NgModule({
  declarations: [ShortNamePipe],
  exports: [ShortNamePipe],
})
export class PipesModule {}
