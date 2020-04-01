import { Injectable } from '@angular/core';
import { Actions, ofType, createEffect } from '@ngrx/effects';
import * as fromActions from './actions';
import { Store, select } from '@ngrx/store';
import { AppModule } from 'src/app/app.module';
import { withLatestFrom, map, mergeMap, catchError } from 'rxjs/operators';
import { selectFolders, selectGroups } from './reducers';
import { of } from 'rxjs';
import { FolderService } from '../services/folders/folder.service';
import { AdminService } from '../services/admin/admin.service';
@Injectable()
export class AdminEffects {
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
  constructor(
    private actions$: Actions,
    private store: Store<AppModule>,
    private foldersService: FolderService,
    private adminService: AdminService
  ) {}
}
