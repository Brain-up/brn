import { createReducer, Action, on, createSelector } from "@ngrx/store";
import { AuthStateModel } from '../models/auth-state.model';
import * as fromAuthActions from './actions';
import { AppStateModel } from 'src/app/models/app-state.model';

export const authFeatureKey = 'auth';
let initialState: AuthStateModel = {
    isAuthenticated: false
};

const _authReducer = createReducer(
    initialState,
    on(fromAuthActions.setAuthStatusAction, (state, action) => ({isAuthenticated: action.isAuthenticated}))
);



// export const selectFeature = (state: )
export function authReducer(state: AuthStateModel, action: Action) {
    return _authReducer(state, action);
}
export const selectAuthFeature = (state: AppStateModel) => state.auth;
export const selectAuthState = createSelector(
    selectAuthFeature,
    (state: AuthStateModel) => state.isAuthenticated
)