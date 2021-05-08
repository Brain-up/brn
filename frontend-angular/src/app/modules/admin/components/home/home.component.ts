import { ChangeDetectionStrategy, Component } from '@angular/core';
import { slideInAnimation } from 'src/app/modules/shared/animations/slide-in-animation';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [slideInAnimation],
})
export class HomeComponent {}
