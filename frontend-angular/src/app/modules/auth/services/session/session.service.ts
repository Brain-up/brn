import { Injectable } from "@angular/core";
import { LoginRequestModel } from '../../models/login-request.model';
import { LoginSuccessModel } from '../../models/login-success.model';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginFailureModel } from '../../models/login-failure.model';

@Injectable()
export class SessionService {
    createSession(loginModel: LoginRequestModel): Observable<LoginSuccessModel | LoginFailureModel> {
        return this.httpClient.post<LoginSuccessModel | LoginFailureModel>('http://10.66.216.143:8081/api/brnlogin', loginModel)
    }
    constructor(private httpClient: HttpClient) {}
}