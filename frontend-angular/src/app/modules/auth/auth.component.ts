import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthenticationApiService } from './services/api/authentication-api.service';

@Component({
    selector: 'app-auth',
    templateUrl: './auth.component.html',
    styleUrls: ['./auth.component.scss'],
    imports: [RouterOutlet, TranslateModule, MatToolbarModule],
    providers: [AuthenticationApiService],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthComponent { }
