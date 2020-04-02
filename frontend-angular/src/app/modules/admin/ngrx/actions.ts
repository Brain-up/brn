import { createAction, props } from '@ngrx/store';
import { Group } from '../model/model';


export const fetchFoldersRequest = createAction(
  '[LoadFileComponent] fetchFoldersRequest'
);
export const fetchFoldersSuccess = createAction(
  '[AdminEffects] fetchFoldersSuccess',
  props<{folders: Array<string>}>()
);
export const fetchFoldersFailure = createAction(
  '[AdminEffects] fetchFoldersFailure',
  props<{error: any}>()
);
export const fetchGroupsRequest = createAction(
  '[LoadFileComponent] fetchGroupsRequest'
);
export const fetchGroupsSuccess = createAction(
  '[AdminEffects] fetchGroupsSuccess',
  props<{groups: Array<Group>}>()
);
export const fetchGroupsFailure = createAction(
  '[AdminEffects] fetchGroupsFailure'
);
