import { createReducer, Action, on, createSelector } from '@ngrx/store';
import { AuthStateModel } from '../models/auth-state.model';
import * as fromAuthActions from './actions';
import { AppStateModel } from 'src/app/models/app-state.model';

export const authFeatureKey = 'auth';
const initialState: AuthStateModel = {
    isAuthenticated: false,
    loginError: undefined
};

const createdAuthReducer = createReducer(
    initialState,
    on(fromAuthActions.setAuthStatusAction, (state, action) => ({isAuthenticated: action.isAuthenticated})),
    on(fromAuthActions.createSessionFailureAction, (state, action) => {
        if (action.statusCode === 500) {
            return {
                ...state,
                loginError: 'Invalid Email field. (Email fields must contain the @ symbol)'
            };
        }
        if (action.statusCode === 401) {
            return {
                ...state,
                loginError: action.errorObj.errors.join('\n')
            };
        }
    }),
    on(fromAuthActions.clearErrorAction, (state, action) => {
        return {
            ...state,
            loginError: undefined
        };
    }),
    on(fromAuthActions.destroySessionSuccessAction, (state, action) => {
        return {
            ...state,
            isAuthenticated: false
        };
    })
);



// export const selectFeature = (state: )
export function authReducer(state: AuthStateModel, action: Action) {
    return createdAuthReducer(state, action);
}
export const selectAuthFeature = (state: AppStateModel) => state.auth;
export const selectAuthState = createSelector(
    selectAuthFeature,
    (state: AuthStateModel) => state.isAuthenticated
);
export const selectAuthError = createSelector(
    selectAuthFeature,
    (state: AuthStateModel) => state.loginError
);
