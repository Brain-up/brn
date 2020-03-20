import { AuthStateModel } from '../modules/auth/models/auth-state.model';
import { AdminStateModel } from '../modules/admin/model/admin-state.model';

export interface AppStateModel {
    auth: AuthStateModel;
    admin: AdminStateModel;
}
