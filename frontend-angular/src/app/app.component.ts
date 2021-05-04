import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { checkAuthStatusAction } from './modules/auth/ngrx/actions';
import { SvgIconsRegistrarService } from './modules/shared/services/svg-icons-registrar.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  constructor(
    private readonly store: Store<any>,
    private readonly svgIconsRegistrarService: SvgIconsRegistrarService
  ) {}

  ngOnInit() {
    this.store.dispatch(checkAuthStatusAction());
    this.svgIconsRegistrarService.registerIcons();
  }
}
