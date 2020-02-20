import { Action, createReducer } from '@ngrx/store';
export interface AdminState {

}
export const initialState: AdminState = {};
const reducer = createReducer(
    initialState,
)
export function adminReducer(state: AdminState, action: Action) {
    return reducer(state, action);
}