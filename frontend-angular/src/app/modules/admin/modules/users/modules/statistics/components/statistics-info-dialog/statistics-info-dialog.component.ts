import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-statistics-info-dialog',
    templateUrl: './statistics-info-dialog.component.html',
    styleUrls: ['./statistics-info-dialog.component.scss'],
    imports: [
        MatIconModule,
        MatDialogModule,
        TranslateModule,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatisticsInfoDialogComponent { }
