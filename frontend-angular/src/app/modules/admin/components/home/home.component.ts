import { Component } from '@angular/core';
import { slideInAnimation } from 'src/app/modules/shared/animations/slideInAnimation';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  animations: [slideInAnimation],
})
export class HomeComponent {
}
