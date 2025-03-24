import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-upload-file',
    templateUrl: './upload-file.component.html',
    styleUrls: ['./upload-file.component.scss'],
    imports: [RouterOutlet, TranslateModule, MatButtonModule],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class UploadFileComponent { }
