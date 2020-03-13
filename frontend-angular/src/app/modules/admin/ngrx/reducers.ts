import { Action, createReducer } from '@ngrx/store';
import { AdminStateModel } from '../model/admin-state.model';
export const adminFeatureKey = 'admin';
export const initialState: AdminStateModel = {};
const reducer = createReducer(
    initialState,
)
export function adminReducer(state: AdminStateModel, action: Action) {
    return reducer(state, action);
}