import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';

import { AppStateModel } from 'src/app/models/app-state.model';
import { destroySessionRequestAction } from '../auth/ngrx/actions';

@Component({
  selector: 'app-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPageComponent {
  constructor(private router: Router, private store: Store<AppStateModel>) {
  }

  logoutUser() {
    this.store.dispatch(destroySessionRequestAction());
  }
}
