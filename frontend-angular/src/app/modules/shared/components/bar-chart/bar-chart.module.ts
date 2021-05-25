import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { BarChartComponent } from './bar-chart.component';

@NgModule({
  declarations: [BarChartComponent],
  imports: [CommonModule, TranslateModule],
  exports: [BarChartComponent],
})
export class BarChartModule {}
