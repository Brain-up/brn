import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SearchInputComponent } from './search-input.component';

@NgModule({
  declarations: [SearchInputComponent],
  imports: [ReactiveFormsModule, MatIconModule],
  exports: [SearchInputComponent],
})
export class SearchInputModule {}
