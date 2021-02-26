import { Action, createReducer, createSelector, on, createFeatureSelector } from '@ngrx/store';
import * as fromAdminActions from '../ngrx/actions';
import { AdminStateModel } from '../model/admin-state.model';

export const adminFeatureKey = 'admin';
export const initialState: AdminStateModel = {};

const reducer = createReducer(
  initialState,
  on(fromAdminActions.fetchFoldersSuccess, (state, action) => ({
    ...state,
    folders: action.folders
  })),
  on(fromAdminActions.fetchGroupsSuccess, (state, action) => ({
    ...state,
    groups: action.groups
  }))
);

export function adminReducer(state: AdminStateModel, action: Action) {
  return reducer(state, action);
}

const selectAdminFeature = createFeatureSelector<AdminStateModel>('admin');

export const selectFolders = createSelector(selectAdminFeature, (adminState) => {
  return adminState.folders;
});

export const selectGroups = createSelector(selectAdminFeature, (adminState) => adminState.groups);
