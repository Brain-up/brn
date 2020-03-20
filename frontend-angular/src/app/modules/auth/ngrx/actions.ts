import { createAction, props } from '@ngrx/store';
import { AuthStateModel } from '../models/auth-state.model';
import { LoginRequestModel } from '../models/login-request.model';
import { LoginSuccessModel } from '../models/login-success.model';
import { LoginFailureModel } from '../models/login-failure.model';


export const checkAuthStatusAction = createAction(
    '[Base Application Page] CheckAuthStatusAction'
);
export const setAuthStatusAction = createAction(
    '[AuthEffects API] SetAuthStatusAction',
    props<AuthStateModel>()
);
export const createSessionRequestAction = createAction(
    '[LoginComponent] Create Session Request Action',
    props<LoginRequestModel>()
);
export const createSessionSuccessAction = createAction(
    '[AuthEffects API] Create Session Success Action',
    props<LoginSuccessModel>()
);
export const createSessionFailureAction = createAction(
    '[AuthEffects API] Create Session Failure Action',
    props<{errorObj: LoginFailureModel, statusCode: number}>()
);
export const clearErrorAction = createAction(
    '[LoginComponent] ClearErrorAction'
);
export const redirectAction = createAction(
    '[AuthEffects API] Redirect to Main Page',
    props<{location: string}>()
);
export const destroySessionRequestAction = createAction(
    '[AdminPageComponent] destroySessionAction'
);
export const destroySessionSuccessAction = createAction(
    '[AuthEffects API] destroySessionSuccessAction'
);
