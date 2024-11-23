import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { BarChartComponent } from './bar-chart.component';

@NgModule({
    imports: [CommonModule, TranslateModule, BarChartComponent],
    exports: [BarChartComponent],
})
export class BarChartModule {}
