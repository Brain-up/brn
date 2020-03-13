import {Component, OnInit} from '@angular/core';
import { Store } from '@ngrx/store';
import { checkAuthStatusAction } from './modules/auth/ngrx/actions';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit{
  title = 'frontend-angular';
  ngOnInit() {
    this.store.dispatch(checkAuthStatusAction());
  }
  constructor(private store: Store<any>){}
}
