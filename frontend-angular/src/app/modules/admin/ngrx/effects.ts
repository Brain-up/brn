import { Injectable } from '@angular/core';

import { Actions, ofType, createEffect } from '@ngrx/effects';
import { Store, select } from '@ngrx/store';
import { withLatestFrom, map, mergeMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import * as fromActions from './actions';
import { AppModule } from 'src/app/app.module';
import { selectFolders, selectGroups } from './reducers';
import { FolderService } from '../services/folders/folder.service';
import { AdminService } from '../services/admin/admin.service';

@Injectable()
export class AdminEffects {
  constructor(
    private actions$: Actions,
    private store: Store<AppModule>,
    private foldersService: FolderService,
    private adminService: AdminService
  ) {
  }

  fetchFoldersRequest = createEffect(() => this.actions$.pipe(
    ofType(fromActions.fetchFoldersRequest),
    withLatestFrom(this.store.pipe(select(selectFolders))),
    mergeMap(([_, folders]) => {
      if (folders) {
        return of(fromActions.fetchFoldersSuccess({folders}));
      }
      return this.foldersService.getFolders().pipe(
        map(receivedFolders => fromActions.fetchFoldersSuccess({folders: receivedFolders})),
        catchError(error => of(fromActions.fetchFoldersFailure(error)))
      );
    })
  ));

  fetchGroupsRequest = createEffect(() => this.actions$.pipe(
    ofType(fromActions.fetchGroupsRequest),
    withLatestFrom(this.store.pipe(select(selectGroups))),
    mergeMap(([_, groups]) => {
      if (groups) {
        return of(fromActions.fetchGroupsSuccess({groups}));
      }
      return this.adminService.getGroups().pipe(
        map(receivedGroups => fromActions.fetchGroupsSuccess({groups: receivedGroups}))
      );
    })
  ));
}
