import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import { destroySessionRequestAction } from '../auth/ngrx/actions';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {
  public readonly mainTabs = [
    { label: 'Users', link: 'users' },
    { label: 'Exercises', link: 'exercises' },
    { label: 'Resources', link: 'resources' },
    { label: 'Upload file', link: 'upload' },
  ];

  constructor(private readonly store: Store<AppStateModel>) {}

  public logoutUser(): void {
    this.store.dispatch(destroySessionRequestAction());
  }
}
