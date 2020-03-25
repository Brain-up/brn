import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LOAD_FILE_PATH, LOAD_TASKS_FILE} from '../shared/app-path';

import { Store } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import { destroySessionRequestAction } from '../auth/ngrx/actions';
import { slideInAnimation } from '../shared/animations/slideInAnimation';


@Component({
  selector: 'app-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss'],
  animations: [slideInAnimation],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPageComponent implements OnInit {
  opened = false;
  ngOnInit() {

  }

  navigate(path: 'file' | 'tasks') {
    this.router.navigate([path === 'file' ? LOAD_FILE_PATH : LOAD_TASKS_FILE]);
  }

  logoutUser() {
    this.store.dispatch(destroySessionRequestAction());
  }

  constructor(private router: Router, private store: Store<AppStateModel>) {}

}
