import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';

import { LOAD_FILE_PATH, LOAD_TASKS_FILE } from '../shared/app-path';
import { AppStateModel } from 'src/app/models/app-state.model';
import { destroySessionRequestAction } from '../auth/ngrx/actions';
import { slideInAnimation } from '../shared/animations/slide-in-animation';

@Component({
  selector: 'app-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss'],
  animations: [slideInAnimation],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPageComponent {
  opened = false;

  constructor(private router: Router, private store: Store<AppStateModel>) {
  }

  navigate(path: 'file' | 'tasks') {
    this.router.navigate([path === 'file' ? LOAD_FILE_PATH : LOAD_TASKS_FILE]);
  }

  logoutUser() {
    this.store.dispatch(destroySessionRequestAction());
  }
}
