import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppStateModel } from './models/app-state.model';
import { checkAuthStatusAction } from './modules/auth/ngrx/actions';
import { SvgIconsRegistrarService } from './modules/shared/services/svg-icons-registrar.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  constructor(
    private readonly store: Store<AppStateModel>,
    private readonly svgIconsRegistrarService: SvgIconsRegistrarService
  ) {}

  ngOnInit() {
    this.store.dispatch(checkAuthStatusAction());
    this.svgIconsRegistrarService.registerIcons();
  }
}
