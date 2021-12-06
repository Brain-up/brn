import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShortNamePipe } from './short-name-pipe.pipe';

@NgModule({
  declarations: [ShortNamePipe],
  // imports: [CommonModule],
  exports: [ShortNamePipe],
})
export class PipesModule {}
