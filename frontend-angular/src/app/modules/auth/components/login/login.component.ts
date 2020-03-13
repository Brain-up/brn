import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import * as fromAuthActions from '../../ngrx/actions';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;


  ngOnInit(): void {
    this.loginForm = new FormGroup({
      grant_type: new FormControl('password'),
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required)
    })
  }
  onLogin() {
    this.store.dispatch(fromAuthActions.createSessionRequestAction(this.loginForm.value))
  }
  constructor(private store: Store<AppStateModel>) { }
}
